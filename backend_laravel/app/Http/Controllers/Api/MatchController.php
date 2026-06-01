<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Matches;
use App\Models\ScoreLog;
use App\Models\Subscriber;
use Google\Auth\Credentials\ServiceAccountCredentials;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Kreait\Firebase\Contract\Messaging;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;


class MatchController extends Controller
{
    public function index()
    {
        $matches = Matches::orderBy('id', 'desc')->get();
        return response()->json($matches);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'team_a'  => 'required|string',
            'team_b'  => 'required|string',
            'score_a' => 'required|integer',
            'score_b' => 'required|integer',
            'status'  => 'in:LIVE,END',
        ]);

        $match = Matches::create($validated);

        return response()->json([
            'success' => true,
            'data'    => $match,
        ], 201);
    }

    public function updateScore(Request $request, $id)
    {
        $validated = $request->validate([
            'poin_a' => 'nullable|integer|min:0',
            'poin_b' => 'nullable|integer|min:0',
        ]);

        $poinA = $validated['poin_a'] ?? 0;
        $poinB = $validated['poin_b'] ?? 0;

        $match = Matches::findOrFail($id);

        // Hitung skor baru
        $newScoreA = $match->score_a + $poinA;
        $newScoreB = $match->score_b + $poinB;

        $match->update([
            'score_a' => $newScoreA,
            'score_b' => $newScoreB,
        ]);

        // Log dengan nilai yang sudah benar
        if ($poinA > 0 || $poinB > 0) {
            ScoreLog::create([
                'match_id' => $match->id,
                'team'     => $poinA > 0 ? $match->team_a : $match->team_b,
                'points'   => $poinA > 0 ? $poinA : $poinB,
                'score_a'  => $newScoreA,  // pakai nilai baru
                'score_b'  => $newScoreB,  // pakai nilai baru
            ]);
        }

        // 3. Ambil semua FCM token subscriber match ini
        $tokens = Subscriber::where('match_id', $match->id)
            ->pluck('fcm_token')
            ->toArray();

        // 4. Push FCM ke semua subscriber
        if (!empty($tokens)) {
            $teamScored = $poinA > 0 ? $match->team_a : ($poinB > 0 ? $match->team_b : ''); // Nama tim yang mencetak poin
            $this->sendFcmToTokens($tokens, [
                'matchId'     => (string) $match->id,
                'team_a'      => $match->team_a,
                'team_b'      => $match->team_b,
                'score_a'     => (string) $match->score_a,
                'score_b'     => (string) $match->score_b,
                'team_scored' => $teamScored,
                'status'      => $match->status,
            ]);
        }

        return response()->json(['success' => true, 'data' => $match]);
    }

    // ── helper: ambil access token dari service account ──
    private function getAccessToken(): string
    {
        $credentialsPath = base_path(env('FIREBASE_CREDENTIALS'));
        $scopes = ['https://www.googleapis.com/auth/firebase.messaging'];

        $credentials = new ServiceAccountCredentials($scopes, $credentialsPath);
        $token = $credentials->fetchAuthToken();

        return $token['access_token'];
    }

    // ── helper: kirim FCM ke banyak token ────────────────
    private function sendFcmToTokens(array $tokens, array $data)
    {
        $projectId   = env('FIREBASE_PROJECT_ID');
        $accessToken = $this->getAccessToken();
        $url         = "https://fcm.googleapis.com/v1/projects/android-kabbadi-rani-tantan/messages:send";

        foreach ($tokens as $token) {
            Http::withHeaders([
                'Authorization' => 'Bearer ' . $accessToken,
                'Content-Type'  => 'application/json',
            ])->post($url, [
                'message' => [
                    'token' => $token,
                    'data'  => $data,
                ]
            ]);
        }
    }


    public function endMatch($id, Messaging $messaging)
    {
        $match = Matches::findOrFail($id);

        $match->update([
            'status' => 'END'
        ]);

        $tokens = $match->subscribers
            ->pluck('fcm_token')
            ->toArray();

        foreach ($tokens as $token) {
            $message = CloudMessage::withTarget('token', $token)
                ->withNotification(
                    Notification::create(
                        'Match Ended',
                        'Pertandingan telah selesai'
                    )
                )
                ->withData([
                    'match_id' => (string)$match->id,
                    'status' => 'END',
                    'score_a' => (string)$match->score_a,
                    'score_b' => (string)$match->score_b
                ]);

            $messaging->send($message);
        }

        return response()->json([
            'message' => 'Match ended'
        ]);
    }

    public function subscribe(Request $request, $id)
    {
        $request->validate([
            'fcm_token' => 'required|string'
        ]);

        $match = Matches::find($id);

        if (!$match) {
            return response()->json([
                'message' => 'Match tidak ditemukan'
            ], 404);
        }

        $subscriber = Subscriber::updateOrCreate(
            [
                'fcm_token' => $request->fcm_token
            ],
            [
                'match_id' => $match->id
            ]
        );

        return response()->json([
            'message' => 'Berhasil subscribe',
            'data' => $subscriber
        ]);
    }

    // Cek apakah token sudah subscribe ke match tertentu
    public function checkSubscription(Request $request, $id)
    {
        $request->validate([
            'fcm_token' => 'required|string'
        ]);

        $subscriber = Subscriber::where('match_id', $id)
            ->where('fcm_token', $request->fcm_token)
            ->first();

        return response()->json([
            'is_subscribed' => $subscriber !== null,
            'data' => $subscriber
        ]);
    }

    public function unsubscribe(Request $request, $id)
    {
        $request->validate([
            'fcm_token' => 'required|string'
        ]);

        $deleted = Subscriber::where('fcm_token', $request->fcm_token)
            ->where('match_id', $id)
            ->delete();

        if (!$deleted) {
            return response()->json([
                'message' => 'Subscriber tidak ditemukan'
            ], 404);
        }

        return response()->json([
            'message' => 'Berhasil unsubscribe'
        ]);
    }

    public function latest()
    {
        $match = Matches::latest()->first();

        if (!$match) {
            return response()->json(['success' => false, 'data' => null], 404);
        }

        return response()->json(['success' => true, 'data' => $match]);
    }


    public function updateToken(Request $request)
    {
        $request->validate([
            'old_token' => 'required|string',
            'new_token' => 'required|string',
        ]);

        $updated = Subscriber::where('fcm_token', $request->old_token)
            ->update(['fcm_token' => $request->new_token]);

        if ($updated === 0) {
            // Tidak ada baris yang diupdate — token lama tidak ditemukan
            // Bukan error, mungkin user belum pernah subscribe
            return response()->json(['message' => 'Tidak ada subscriber dengan token tersebut'], 200);
        }

        return response()->json(['message' => 'Token berhasil diperbarui'], 200);
    }

    public function detail($matchId)
    {
        $match = Matches::findOrFail($matchId);

        $scoreLogs = ScoreLog::where('match_id', $matchId)
            ->orderBy('created_at', 'desc')
            ->get();            

        return response()->json([
            'data' => [
                'id'         => $match->id,
                'team_a'     => $match->team_a,
                'team_b'     => $match->team_b,
                'score_a'    => $match->score_a,
                'score_b'    => $match->score_b,
                'status'     => $match->status,
                'match_time' => $match->created_at->format('H:i'),
                'last_updates' => $scoreLogs->map(fn($log) => [
                    'team'    => $log->team,
                    'points'  => $log->points,
                    'score_a' => $log->score_a,
                    'score_b' => $log->score_b,
                    'time'    => \Carbon\Carbon::parse($log->created_at)->format('H:i:s'),
                ])
            ]
        ]);
    }

}
