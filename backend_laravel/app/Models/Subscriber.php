<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Subscriber extends Model
{
    protected $fillable = [
        'match_id',
        'fcm_token'
    ];

    public function match()
    {
        return $this->belongsTo(Matches::class);
    }
}
