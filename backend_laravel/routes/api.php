<?php

use App\Http\Controllers\Api\MatchController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

// Route::get('/user', function (Request $request) {
//     return $request->user();
// })->middleware('auth:sanctum');

Route::get('/match', [MatchController::class, 'index']);
Route::post('/match/{id}/subscribe', [MatchController::class, 'subscribe']);
Route::post('/match/{id}/score', [MatchController::class, 'updateScore']);
Route::post('/match/{id}/end', [MatchController::class, 'endMatch']);
