const express = require('express');
const admin = require('firebase-admin');
const app = express();

app.use(express.json());

// ─────────────────────────────────────────────
// Firebase Admin SDK initialization
// Download your service account key JSON from:
//   Firebase Console → Project Settings → Service Accounts → Generate new private key
// Save it as "serviceAccountKey.json" in this directory.
// ─────────────────────────────────────────────
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// ─────────────────────────────────────────────
// In-memory data store (replace with a real DB in production)
// ─────────────────────────────────────────────
const matches = [
  { id: '1', teamA: 'India', teamB: 'Pakistan', scoreA: 0, scoreB: 0, status: 'LIVE' },
  { id: '2', teamA: 'Iran',  teamB: 'Bangladesh', scoreA: 0, scoreB: 0, status: 'LIVE' },
  { id: '3', teamA: 'Nepal', teamB: 'Sri Lanka', scoreA: 0, scoreB: 0, status: 'FINISHED' },
];

// Map of matchId → array of FCM tokens subscribed to that match
const subscriptions = {};

// ─────────────────────────────────────────────
// GET /match  →  list all matches
// ─────────────────────────────────────────────
app.get('/match', (req, res) => {
  res.json(matches);
});

// ─────────────────────────────────────────────
// POST /match/:id/subscribe
// Body: { fcmToken: "..." }
// Registers the device token for push notifications.
// ─────────────────────────────────────────────
app.post('/match/:id/subscribe', (req, res) => {
  const matchId = req.params.id;
  const { fcmToken } = req.body;

  if (!fcmToken) {
    return res.status(400).json({ error: 'fcmToken is required' });
  }

  const match = matches.find(m => m.id === matchId);
  if (!match) {
    return res.status(404).json({ error: 'Match not found' });
  }
  if (match.status !== 'LIVE') {
    return res.status(400).json({ error: 'Match is not LIVE' });
  }

  if (!subscriptions[matchId]) subscriptions[matchId] = [];

  // Avoid duplicate tokens
  if (!subscriptions[matchId].includes(fcmToken)) {
    subscriptions[matchId].push(fcmToken);
  }

  console.log(`Device subscribed to match ${matchId}. Total subscribers: ${subscriptions[matchId].length}`);
  res.json({ message: 'Subscribed successfully', match });
});

// ─────────────────────────────────────────────
// POST /match/:id/score
// Body: { team: "A" | "B" }
// Increments the score for the given team and sends FCM to all subscribers.
// ─────────────────────────────────────────────
app.post('/match/:id/score', async (req, res) => {
  const matchId = req.params.id;
  const { team } = req.body;

  if (!team || !['A', 'B'].includes(team)) {
    return res.status(400).json({ error: 'team must be "A" or "B"' });
  }

  const match = matches.find(m => m.id === matchId);
  if (!match) return res.status(404).json({ error: 'Match not found' });
  if (match.status !== 'LIVE') return res.status(400).json({ error: 'Match is not LIVE' });

  const scoringTeam = team === 'A' ? match.teamA : match.teamB;
  if (team === 'A') match.scoreA += 1;
  else match.scoreB += 1;

  console.log(`Score update: ${match.teamA} ${match.scoreA} - ${match.scoreB} ${match.teamB}`);

  await sendScoreUpdateNotification(matchId, match, scoringTeam);
  res.json({ message: 'Score updated', match });
});

// ─────────────────────────────────────────────
// POST /match/:id/stop
// Marks match as FINISHED and sends FCM notification.
// ─────────────────────────────────────────────
app.post('/match/:id/stop', async (req, res) => {
  const matchId = req.params.id;
  const match = matches.find(m => m.id === matchId);

  if (!match) return res.status(404).json({ error: 'Match not found' });
  if (match.status === 'FINISHED') return res.status(400).json({ error: 'Match already finished' });

  match.status = 'FINISHED';
  console.log(`Match ${matchId} finished. Final: ${match.teamA} ${match.scoreA} - ${match.scoreB} ${match.teamB}`);

  await sendMatchFinishedNotification(matchId, match);
  res.json({ message: 'Match stopped', match });
});

// ─────────────────────────────────────────────
// Helper: send score update FCM to all subscribers
// ─────────────────────────────────────────────
async function sendScoreUpdateNotification(matchId, match, scoringTeam) {
  const tokens = subscriptions[matchId] || [];
  if (tokens.length === 0) return;

  const message = {
    data: {
      scoreA: String(match.scoreA),
      scoreB: String(match.scoreB),
      scoringTeam: scoringTeam,
      status: match.status,
    },
    notification: {
      title: '⚡ Skor Diperbarui!',
      body: `${scoringTeam} mencetak poin! Skor: ${match.scoreA} - ${match.scoreB}`,
    },
    tokens: tokens,
  };

  try {
    const response = await admin.messaging().sendEachForMulticast(message);
    console.log(`FCM sent: ${response.successCount} success, ${response.failureCount} failed`);
    removeInvalidTokens(matchId, tokens, response.responses);
  } catch (err) {
    console.error('FCM error:', err.message);
  }
}

// ─────────────────────────────────────────────
// Helper: send match-finished FCM to all subscribers
// ─────────────────────────────────────────────
async function sendMatchFinishedNotification(matchId, match) {
  const tokens = subscriptions[matchId] || [];
  if (tokens.length === 0) return;

  const message = {
    data: {
      scoreA: String(match.scoreA),
      scoreB: String(match.scoreB),
      scoringTeam: '',
      status: 'FINISHED',
    },
    notification: {
      title: 'Pertandingan Berakhir',
      body: `Skor akhir: ${match.teamA} ${match.scoreA} - ${match.scoreB} ${match.teamB}`,
    },
    tokens: tokens,
  };

  try {
    const response = await admin.messaging().sendEachForMulticast(message);
    console.log(`FCM sent: ${response.successCount} success, ${response.failureCount} failed`);
    removeInvalidTokens(matchId, tokens, response.responses);
  } catch (err) {
    console.error('FCM error:', err.message);
  }
}

// ─────────────────────────────────────────────
// Helper: clean up invalid/expired FCM tokens
// ─────────────────────────────────────────────
function removeInvalidTokens(matchId, tokens, responses) {
  const toRemove = responses
    .map((r, i) => (!r.success ? tokens[i] : null))
    .filter(Boolean);
  if (toRemove.length > 0) {
    subscriptions[matchId] = subscriptions[matchId].filter(t => !toRemove.includes(t));
    console.log(`Removed ${toRemove.length} invalid token(s) for match ${matchId}`);
  }
}

// ─────────────────────────────────────────────
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Kabaddi backend running on port ${PORT}`);
  console.log('Endpoints:');
  console.log('  GET  /match');
  console.log('  POST /match/:id/subscribe   { fcmToken }');
  console.log('  POST /match/:id/score       { team: "A"|"B" }');
  console.log('  POST /match/:id/stop');
});
