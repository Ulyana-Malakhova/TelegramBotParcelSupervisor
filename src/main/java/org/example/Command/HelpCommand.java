package org.example.Command;

public class HelpCommand {

    public String getHelpMessage() {
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("Доступные команды:\n");
        helpMessage.append("/help - Выводит справочный текст\n");
        helpMessage.append("/about - Выводит информацию о боте и разработчиках\n");
        helpMessage.append("/start - Запускает бота\n");
        return helpMessage.toString();
    }
}