<?php

use App\Http\Controllers\Api\MatchController;
use App\Http\Controllers\Api\ScoreLogController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

// Route::get('/user', function (Request $request) {
//     return $request->user();
// })->middleware('auth:sanctum');

Route::get('/match', [MatchController::class, 'index']);
Route::post('/match/{id}/subscribe', [MatchController::class, 'subscribe']);
Route::get('/match/{id}/check-subscription', [MatchController::class, 'checkSubscription']);
Route::delete('/match/{id}/unsubscribe', [MatchController::class, 'unsubscribe']);
Route::put('/match/{id}/score', [MatchController::class, 'updateScore']);
Route::post('/match/{id}/end', [MatchController::class, 'endMatch']);
Route::post('/match/save', [MatchController::class, 'store']);
Route::post('/score-logs', [ScoreLogController::class, 'store']);
Route::get('/score-logs', [ScoreLogController::class, 'index']);
Route::get('/match/latest', [MatchController::class, 'latest']);
