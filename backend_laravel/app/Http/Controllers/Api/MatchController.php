<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Matches;
use App\Models\Subscriber;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Messaging;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;


class MatchController extends Controller
{
    public function index()
    {
        $matches = Matches::all();
        return response()->json($matches);
    }

    public function updateScore($id, Request $request, Messaging $messaging)
    {
        $match = Matches::findOrFail($id);
        $team = $request->input('team');

        if ($team === 'A') {
            $match->increment('score_a');

            $teamName = $match->team_a;
            $newScore = $match->score_a;
        } else {
            $match->increment('score_b');

            $teamName = $match->team_b;
            $newScore = $match->score_b;
        }

        $match->refresh();

        $tokens = $match->subscribers
            ->pluck('fcm_token')
            ->toArray();

        foreach ($tokens as $token) {
            $message = CloudMessage::withTarget('token', $token)
                ->withNotification(
                    Notification::create(
                        'Score Updated',
                        "$teamName mencetak skor! Skor baru: $newScore"
                    )
                )
                ->withData([
                    'match_id' => (string)$match->id,
                    'team_a' => $match->team_a,
                    'team_b' => $match->team_b,
                    'score_a' => (string)$match->score_a,
                    'score_b' => (string)$match->score_b,
                    'status' => $match->status
                ]);

            $messaging->send($message);
        }

        return response()->json($match);
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

}
