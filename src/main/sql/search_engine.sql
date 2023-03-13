-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Хост: db
-- Время создания: Мар 13 2023 г., 12:51
-- Версия сервера: 8.0.32
-- Версия PHP: 8.1.15

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `search_engine`
--

-- --------------------------------------------------------

--
-- Структура таблицы `index`
--

CREATE TABLE `index` (
  `id` int NOT NULL,
  `rank` float NOT NULL,
  `lemma_id` int NOT NULL,
  `page_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ru_0900_ai_ci;

-- --------------------------------------------------------

--
-- Структура таблицы `lemma`
--

CREATE TABLE `lemma` (
  `id` int NOT NULL,
  `frequency` int NOT NULL,
  `lemma` varchar(255) COLLATE utf8mb4_ru_0900_ai_ci NOT NULL,
  `site_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ru_0900_ai_ci;

-- --------------------------------------------------------

--
-- Структура таблицы `page`
--

CREATE TABLE `page` (
  `id` int NOT NULL,
  `code` int NOT NULL,
  `content` mediumtext COLLATE utf8mb4_ru_0900_ai_ci NOT NULL,
  `path` text COLLATE utf8mb4_ru_0900_ai_ci NOT NULL,
  `site_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ru_0900_ai_ci;

-- --------------------------------------------------------

--
-- Структура таблицы `site`
--

CREATE TABLE `site` (
  `id` int NOT NULL,
  `last_error` text COLLATE utf8mb4_ru_0900_ai_ci,
  `name` varchar(255) COLLATE utf8mb4_ru_0900_ai_ci NOT NULL,
  `status` enum('INDEXING','INDEXED','FAILED') COLLATE utf8mb4_ru_0900_ai_ci NOT NULL,
  `status_time` datetime NOT NULL,
  `url` varchar(255) COLLATE utf8mb4_ru_0900_ai_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ru_0900_ai_ci;

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `index`
--
ALTER TABLE `index`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_INDEX_LEMMA_ID` (`lemma_id`),
  ADD KEY `FK_INDEX_PAGE_ID` (`page_id`);

--
-- Индексы таблицы `lemma`
--
ALTER TABLE `lemma`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_LEMMA_SITE_ID` (`site_id`);

--
-- Индексы таблицы `page`
--
ALTER TABLE `page`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_PAGE_PATH` (`path`(512)),
  ADD KEY `FK_PAGE_SITE_ID` (`site_id`);

--
-- Индексы таблицы `site`
--
ALTER TABLE `site`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `index`
--
ALTER TABLE `index`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT для таблицы `lemma`
--
ALTER TABLE `lemma`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT для таблицы `page`
--
ALTER TABLE `page`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT для таблицы `site`
--
ALTER TABLE `site`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `index`
--
ALTER TABLE `index`
  ADD CONSTRAINT `FK_INDEX_LEMMA_ID` FOREIGN KEY (`lemma_id`) REFERENCES `lemma` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `FK_INDEX_PAGE_ID` FOREIGN KEY (`page_id`) REFERENCES `page` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `lemma`
--
ALTER TABLE `lemma`
  ADD CONSTRAINT `FK_LEMMA_SITE_ID` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `page`
--
ALTER TABLE `page`
  ADD CONSTRAINT `FK_PAGE_SITE_ID` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
