// @ts-nocheck
app.get('/data', async (req, res) => {
  try {
    const data = await fetchData();
    res.json(data);
  } catch (err) {
    res.status(500).json({ error: 'server error' });
  }
});
