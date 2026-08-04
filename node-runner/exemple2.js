app.get('/users/:id', (req, res) => {
  if (!req.params.id) {
    return res.status(400).json({ error: 'missing id' });
  }
  res.json({ id: req.params.id });
});

function addition(a, b) {
  return a + b;
}
