<?php

namespace App\Http\Controllers\Api;

use App\Models\ScoreLog;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class ScoreLogController extends Controller
{
    public function store(Request $request)
    {
        $validated = $request->validate([
            'match_id' => 'nullable|exists:matches,id',
            'team'     => 'required|in:team_a,team_b',
            'points'   => 'required|integer|in:1,2',
            'score_a'  => 'nullable|integer',
            'score_b'  => 'nullable|integer',
        ]);

        $log = ScoreLog::create($validated);

        return response()->json(['success' => true, 'data' => $log], 201);
    }

    public function index(Request $request)
    {
        $logs = ScoreLog::when($request->match_id, fn($q) => $q->where('match_id', $request->match_id))
            ->latest()
            ->get();

        return response()->json(['success' => true, 'data' => $logs]);
    }
}
