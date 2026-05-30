<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Matches extends Model
{
    protected $table = 'matches';
    protected $fillable = [
        'team_a',
        'team_b',
        'score_a',
        'score_b',
        'status'
    ];

    public function subscribers()
    {
        return $this->hasMany(Subscriber::class);
    }
}
