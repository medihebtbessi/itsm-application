# chatbot_api.py
from flask import Flask, request, jsonify
from langchain_community.llms import Ollama
from langchain_core.prompts import ChatPromptTemplate

app = Flask(__name__)

template = """
Answer the question below.

Here is the conversation history:
{context}

Question: {question}

Answer:
"""

model = Ollama(model="llama3")
prompt = ChatPromptTemplate.from_template(template)
chain = prompt | model

@app.route("/chat", methods=["POST"])
def chat():
    data = request.json
    context = data.get("context", "")
    question = data["question"]
    result = chain.invoke({"context": context, "question": question})
    return jsonify({"response": result})

if __name__ == "__main__":
    app.run(port=5000)
