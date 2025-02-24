package com.example.kasperchat_test

import com.example.kasperchat_test.ui.adapter.message.MessageItem
import com.example.kasperchat_test.ui.adapter.message.MessageType

fun fillMessageItemsWithTestData(messagesItems: MutableList<MessageItem>) {
    messagesItems.add(
        MessageItem(
            messageId = "1",
            text = "Привет!",
            timestamp = System.currentTimeMillis() - 120000, // 2 минуты назад
            authorId = "user1",
            isRead = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "вфыапывфаирывр",
            text = "Как дела?",
            timestamp = System.currentTimeMillis() - 120000, // 2 минуты назад
            authorId = "user1",
            isRead = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "выапыва",
            text = "Прошла минута чел, а ты всё молчишь",
            timestamp = System.currentTimeMillis() - 60000, // 1 минута назад
            authorId = "user1",
            isRead = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "2",
            text = "Привет! Все отлично, а у тебя?",
            timestamp = System.currentTimeMillis() - 30000, // 30 секунд назад
            authorId = "null",
            isRead = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "3",
            text = "Тоже хорошо. Чем занимаешься?",
            timestamp = System.currentTimeMillis() - 10000, // 10 секунд назад
            authorId = "user1",
            isRead = false,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "4",
            text = "Работаю над проектом.",
            timestamp = System.currentTimeMillis() - 5000, // 5 секунд назад
            authorId = "null",
            isRead = false,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "sdfgbhfgjhn",
            text = "А мог бы нормально отдыхать",
            timestamp = System.currentTimeMillis() - 4000, // 4 секунд назад
            authorId = "null",
            isRead = false,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "5",
            text = "Ладно прости",
            timestamp = System.currentTimeMillis() - 3000, // 3 секунд назад
            authorId = "user1",
            isRead = true,
            isEdited = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "6",
            text = "Я тебя прощаю",
            timestamp = System.currentTimeMillis() - 2000, // 2 секунд назад
            authorId = "null",
            isRead = true,
            isDeleted = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "7",
            text = "Это сообщение является ответом на сообщение с id 1",
            timestamp = System.currentTimeMillis() - 1000, // 1 секунда назад
            authorId = "user1",
            isRead = true,
            replyToMessageId = "1",
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "ывапрытапт",
            text = "Но пока что не реализовано",
            timestamp = System.currentTimeMillis() - 900, // 0,9 секунда назад
            authorId = "user1",
            isRead = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "ывпыврырп",
            text = "Ща видос снизу скину, чекни",
            timestamp = System.currentTimeMillis(), // Сейчас
            authorId = "user1",
            isRead = true,
            type = MessageType.TEXT
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "8",
            text = "Это сообщение с картинкой",
            timestamp = System.currentTimeMillis(), // 5 минут назад
            authorId = "user1",
            isRead = true,
            imageUrl = "https://via.placeholder.com/150",
            type = MessageType.IMAGE
        )
    )
    messagesItems.add(
        MessageItem(
            messageId = "9",
            text = "Это сообщение с видео",
            timestamp = System.currentTimeMillis(), // 6 минут назад
            authorId = "user1",
            isRead = true,
            imageUrl = "https://via.placeholder.com/150",
            type = MessageType.VIDEO
        )
    )
}