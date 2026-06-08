<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ScoreLog extends Model
{
    protected $fillable = [
        'match_id', 
        'team', 
        'points', 
        'score_a', 
        'score_b'
        ];

    public function match()
    {
        return $this->belongsTo(Matches::class, 'match_id');
    }
}
