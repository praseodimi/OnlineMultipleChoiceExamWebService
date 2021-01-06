package rmi.server;

import java.util.ArrayList;

public class Quiz {
    private int id;
    private String question;
    private ArrayList<String> choices;
    private int correctAnswer;
    private Integer selectedChoice;

    public Quiz(int id, String question, ArrayList<String> choices, int correctAnswer, Integer selectedChoice) {
        this.id = id;
        this.question = question;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.selectedChoice = selectedChoice;
    }

    public int getCorrectAnswer() {
        return this.correctAnswer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(question).append("\n");
        for (int i = 0; i < choices.size(); i++) {
            sb.append((i + 1)).append(") ").append(choices.get(i)).append("\n");
        }
        return sb.toString();
    }

    public Integer getSelectedChoice() {
        return selectedChoice;
    }

    public void setSelectedChoice(Integer selectedChoice) {
        this.selectedChoice = selectedChoice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<String> getChoices() {
        return choices;
    }

    public void setChoices(ArrayList<String> choices) {
        this.choices = choices;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}