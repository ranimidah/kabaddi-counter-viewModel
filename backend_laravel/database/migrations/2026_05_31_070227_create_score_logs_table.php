<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('score_logs', function (Blueprint $table) {
            $table->id();
            $table->foreignId('match_id')->nullable()->constrained('matches')->nullOnDelete();
            $table->string('team');        // "team_a" atau "team_b"
            $table->integer('points');     // 1 atau 2
            $table->integer('score_a');    // skor A setelah perubahan
            $table->integer('score_b');    // skor B setelah perubahan
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('score_logs');
    }
};
