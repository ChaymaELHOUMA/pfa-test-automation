const express = require("express");
const router = express.Router();
const { add, subtract, multiply, divide } = require("../src/utils");

function parseNumbers(req, res) {
  const a = Number(req.query.a);
  const b = Number(req.query.b);
  if (Number.isNaN(a) || Number.isNaN(b)) {
    res.status(400).json({ error: "Les paramètres a et b doivent être des nombres" });
    return null;
  }
  return { a, b };
}

// GET /api/calculator/add?a=1&b=2
router.get("/add", (req, res) => {
  const nums = parseNumbers(req, res);
  if (!nums) return;
  res.status(200).json({ result: add(nums.a, nums.b) });
});

// GET /api/calculator/subtract?a=1&b=2
router.get("/subtract", (req, res) => {
  const nums = parseNumbers(req, res);
  if (!nums) return;
  res.status(200).json({ result: subtract(nums.a, nums.b) });
});

// GET /api/calculator/multiply?a=1&b=2
router.get("/multiply", (req, res) => {
  const nums = parseNumbers(req, res);
  if (!nums) return;
  res.status(200).json({ result: multiply(nums.a, nums.b) });
});

// GET /api/calculator/divide?a=1&b=2
router.get("/divide", (req, res) => {
  const nums = parseNumbers(req, res);
  if (!nums) return;
  try {
    res.status(200).json({ result: divide(nums.a, nums.b) });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

module.exports = router;
