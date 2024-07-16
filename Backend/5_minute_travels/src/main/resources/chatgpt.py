import ast
import os
import re
import sys
from os import path
from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain_chroma import Chroma
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_community.document_loaders import TextLoader
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_mongodb.chat_message_histories import MongoDBChatMessageHistory
import logging


os.environ["OPENAI_API_KEY"] = "Your key"

llm = ChatOpenAI(model="gpt-3.5-turbo", temperature=0)

loader = TextLoader("/app/data.txt")
docs = loader.load()

text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
splits = text_splitter.split_documents(docs)
vectorstore = Chroma.from_documents(documents=splits, embedding=OpenAIEmbeddings())
retriever = vectorstore.as_retriever()
logging.getLogger().setLevel(logging.ERROR) # hide warning log

contextualize_q_system_prompt = """Given a chat history and the latest user question \
which might reference context in the chat history, formulate a standalone question \
which can be understood without the chat history. Do NOT answer the question, \
just reformulate it if needed and otherwise return it as is."""
contextualize_q_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", contextualize_q_system_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ]
)
history_aware_retriever = create_history_aware_retriever(
    llm, retriever, contextualize_q_prompt
)

qa_system_prompt = """You are an assistant for question-answering tasks. \
Use the following pieces of retrieved context to answer the question. \
If you don't know the answer, just say that you don't know. \
Use three sentences maximum and keep the answer concise.\

{context}"""
qa_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", qa_system_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ]
)
question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)

rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)


session_id=sys.argv[2]
conversational_rag_chain = RunnableWithMessageHistory(
    rag_chain,
    lambda session_id: MongoDBChatMessageHistory(
        session_id=session_id,
        connection_string="Your url",
        database_name="test",
        collection_name="chat_history"
    ),
    input_messages_key="input",
    history_messages_key="chat_history",
    output_messages_key="answer",
)

def main():

    query = sys.argv[1]
    if query is None:
        print("Try again")
        sys.exit(0)
    result = conversational_rag_chain.invoke(
        {"input": query},
        config={"configurable": {"session_id": session_id}})  

    response = result["answer"]
    print(response)
    sys.exit(0)
    


if __name__ == "__main__":
    main()
