package com.arn.ycyw.your_car_your_way.services.impl;
import com.arn.ycyw.your_car_your_way.services.OpenAiChatService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class OpenAiChatServiceImpl implements OpenAiChatService {


    private final OpenAIClient client;

    public OpenAiChatServiceImpl() {
        System.out.println("OPENAI_API_KEY (vue par la JVM) = " + System.getenv("OPENAI_API_KEY"));
        this.client = OpenAIOkHttpClient.fromEnv();
    }

    public String chat(String userMessage) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_5_1)
                .addSystemMessage("""
                Tu es l'assistant conversationnel de l'application "Your Car Your Way".
                - Tu réponds uniquement en français.
                - Tu aides l'utilisateur à propos des voitures, des trajets, de la location et des fonctionnalités de l'application.
                - Si la question sort de ce cadre (politique, médecine, sujets perso, etc.),
                  tu réponds poliment que tu n'es pas conçu pour ça et tu recentres sur l'application.
                - Tu t'exprimes de façon claire, pédagogique et concise.
                """)
                .addUserMessage(userMessage)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        if (completion.choices().isEmpty()) {
            return "Aucune réponse renvoyée par le modèle.";
        }

        return completion.choices()
                .get(0)
                .message()
                .content()
                .orElse("Réponse vide du modèle");
    }
}
