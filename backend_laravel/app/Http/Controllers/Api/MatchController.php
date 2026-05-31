<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Matches;
use App\Models\ScoreLog;
use App\Models\Subscriber;
use Illuminate\Http\Request;
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

    // public function updateScore($id, Request $request, Messaging $messaging)
    // {
    //     $match = Matches::findOrFail($id);
    //     $team = $request->input('team');

    //     if ($team === 'A') {
    //         $match->increment('score_a');

    //         $teamName = $match->team_a;
    //         $newScore = $match->score_a;
    //     } else {
    //         $match->increment('score_b');

    //         $teamName = $match->team_b;
    //         $newScore = $match->score_b;
    //     }

    //     $match->refresh();

    //     $tokens = $match->subscribers
    //         ->pluck('fcm_token')
    //         ->toArray();

    //     foreach ($tokens as $token) {
    //         $message = CloudMessage::withTarget('token', $token)
    //             ->withNotification(
    //                 Notification::create(
    //                     'Score Updated',
    //                     "$teamName mencetak skor! Skor baru: $newScore"
    //                 )
    //             )
    //             ->withData([
    //                 'match_id' => (string)$match->id,
    //                 'team_a' => $match->team_a,
    //                 'team_b' => $match->team_b,
    //                 'score_a' => (string)$match->score_a,
    //                 'score_b' => (string)$match->score_b,
    //                 'status' => $match->status
    //             ]);

    //         $messaging->send($message);
    //     }

    //     return response()->json($match);
    // }

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

        return response()->json(['success' => true, 'data' => $match]);
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

}
