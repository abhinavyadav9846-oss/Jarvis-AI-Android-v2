import express from "express";
import cors from "cors";
import OpenAI from "openai";
import "dotenv/config";

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});

app.get("/", (req, res) => {
  res.json({
    status: "online",
    service: "JARVIS AI Backend"
  });
});

app.post("/chat", async (req, res) => {
  try {
    const message = req.body?.message?.trim();

    if (!message) {
      return res.status(400).json({
        error: "Message is required"
      });
    }

    const response = await openai.responses.create({
      model: process.env.OPENAI_MODEL || "gpt-5.6-luna",
      instructions:
        "You are JARVIS, a helpful AI assistant. " +
        "Answer clearly and naturally. " +
        "You can communicate in Hindi, English, or Hinglish.",
      input: message
    });

    res.json({
      reply: response.output_text
    });

  } catch (error) {
    console.error("JARVIS AI error:", error);

    res.status(500).json({
      error: "JARVIS AI could not process the request."
    });
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`JARVIS backend running on port ${port}`);
});
