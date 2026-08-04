app.post('/users', authMiddleware, validationMiddleware, (req, res) => {
  res.status(201).json({ ok: true });
});
