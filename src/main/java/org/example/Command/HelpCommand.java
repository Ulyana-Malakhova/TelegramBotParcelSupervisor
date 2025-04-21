package org.example.Command;

public class HelpCommand {

    public String getHelpMessage() {
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("Доступные команды:\n");
        helpMessage.append("/help - Выводит справочный текст\n");
        helpMessage.append("/about - Выводит информацию о боте и разработчиках\n");
        helpMessage.append("/start - Запускает бота\n");
        helpMessage.append("/track - Получение информации о статусе посылки, месте ее нахождения и времени изменения статуса(через пробел вводится трек-номер или название посылки)\n");
        helpMessage.append("/history - Получение истории всех передвижений посылки(через пробел вводится трек-номер или название посылки)\n");
        helpMessage.append("/saved_parcels - Получение трек-номера и статуса всех активных отслеживаемых посылок\n");
        helpMessage.append("/auth - Авторизация администратора\n");
        helpMessage.append("/delete_name - Удаление краткого названия для посылки(через пробел вводится трек-номер или название посылки)\n");
        helpMessage.append("/add_name - Добавление краткого названия для посылки(через пробел вводится трек-номер и название посылки)\n");
        helpMessage.append("/traceability_track - Изменение статуса отслеживаемости посылки(через пробел вводится трек-номер или название посылки)\n");
        helpMessage.append("/report - Получение отчетов об отправленных/полученных посылках за указанный период\n");
        helpMessage.append("/recent_tracks - Просмотр последних пяти посылок\n");
        return helpMessage.toString();
    }
}