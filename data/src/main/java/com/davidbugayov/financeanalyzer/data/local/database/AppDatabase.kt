package com.davidbugayov.financeanalyzer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.data.local.converter.DateConverter
import com.davidbugayov.financeanalyzer.data.local.converter.MoneyConverter
import com.davidbugayov.financeanalyzer.data.local.converter.StringListConverter
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

/**
 * Класс базы данных Room для приложения.
 * Содержит таблицу транзакций.
 */
@Database(
    entities = [
        TransactionEntity::class,
    ],
    version = 16,
    exportSchema = true,
)
@TypeConverters(DateConverter::class, MoneyConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Предоставляет доступ к DAO для работы с транзакциями
     */
    abstract fun transactionDao(): TransactionDao

    companion object {

        private const val DATABASE_NAME = "finance_analyzer.db"

        /**
         * Миграция с версии 1 на версию 2
         * Добавляет поле currencyCode в таблицу transactions
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL(
                        "ALTER TABLE transactions ADD COLUMN currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}'",
                    )
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_1_2", "Ошибка миграции с 1 на 2", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 2 на версию 3
         * Делает поле title необязательным
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Создаем временную таблицу с новой схемой
                    db.execSQL(
                        """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount REAL NOT NULL,
                        currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}',
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT
                    )
                """,
                    )

                    // Копируем данные из старой таблицы в новую
                    db.execSQL(
                        """
                    INSERT INTO transactions_temp (id, amount, currencyCode, category, isExpense, date, note)
                    SELECT id, amount, currencyCode, category, isExpense, date, note FROM transactions
                """,
                    )

                    // Удаляем старую таблицу
                    db.execSQL("DROP TABLE transactions")

                    // Переименовываем временную таблицу
                    db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_2_3", "Ошибка миграции с 2 на 3", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 3 на версию 4
         * Добавляет поле source в таблицу transactions
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Добавляем колонку source с значением по умолчанию "Наличные"
                    db.execSQL(
                        "ALTER TABLE transactions ADD COLUMN source TEXT NOT NULL DEFAULT 'Наличные'",
                    )
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_3_4", "Ошибка миграции с 3 на 4", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 4 на версию 5
         * Добавляет поле destination в таблицу transactions
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Добавляем колонку destination с значением по умолчанию "Наличные"
                    db.execSQL(
                        "ALTER TABLE transactions ADD COLUMN destination TEXT NOT NULL DEFAULT 'Наличные'",
                    )
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_4_5", "Ошибка миграции с 4 на 5", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 5 на версию 6
         * Изменяет тип поля amount с REAL на TEXT для более точного хранения денежных значений
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Создаем временную таблицу с новой схемой
                    db.execSQL(
                        """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount TEXT NOT NULL,
                        currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}',
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Сбер',
                        destination TEXT NOT NULL DEFAULT 'Наличные'
                    )
                """,
                    )

                    // Копируем данные из старой таблицы в новую, преобразуя amount в TEXT
                    db.execSQL(
                        """
                    INSERT INTO transactions_temp (id, amount, currencyCode, category, isExpense, date, note, source, destination)
                    SELECT id, CAST(amount AS TEXT), currencyCode, category, isExpense, date, note, source, destination FROM transactions
                """,
                    )

                    // Удаляем старую таблицу
                    db.execSQL("DROP TABLE transactions")

                    // Переименовываем временную таблицу
                    db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_5_6", "Ошибка миграции с 5 на 6", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 6 на версию 7
         * Оставляет пустую миграцию, т.к. таблица financial_goals больше не нужна
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Миграция не требуется
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_6_7", "Ошибка миграции с 6 на 7", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 7 на версию 8
         * Исправляет структуру таблицы transactions, приводя ее в соответствие с TransactionEntity
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Создаем временную таблицу с правильной схемой
                    db.execSQL(
                        """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Сбер'
                    )
                    """,
                    )

                    // Копируем данные из старой таблицы в новую, игнорируя лишние колонки
                    db.execSQL(
                        """
                    INSERT INTO transactions_temp (id, amount, category, isExpense, date, note, source)
                    SELECT id, amount, category, isExpense, date, note, source FROM transactions
                    """,
                    )

                    // Удаляем старую таблицу
                    db.execSQL("DROP TABLE transactions")

                    // Переименовываем временную таблицу
                    db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_7_8", "Ошибка миграции с 7 на 8", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 8 на версию 9
         * Обновляет схему таблицы после изменения значений по умолчанию для source
         */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Эта миграция не изменяет структуру таблицы,
                    // а только синхронизирует схему Room с фактической схемой базы данных
                    // Можно добавить пустой блок или выполнить SQL для обновления данных
                    // Например, обновить источник "Сбер" на "Наличные" если нужно
                    db.execSQL("UPDATE transactions SET source = 'Наличные' WHERE source = 'Сбер'")
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_8_9", "Ошибка миграции с 8 на 9", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 9 на версию 10
         * Добавляет поле sourceColor в таблицу transactions
         */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Добавляем колонку sourceColor с NULL значением по умолчанию
                    db.execSQL("ALTER TABLE transactions ADD COLUMN sourceColor INTEGER")

                    // Обновляем цвета для стандартных источников
                    db.execSQL("UPDATE transactions SET sourceColor = 0xFF21A038 WHERE source = 'Сбер'")
                    db.execSQL(
                        "UPDATE transactions SET sourceColor = 0xFFFFDD2D WHERE source = 'Т-Банк'",
                    )
                    db.execSQL(
                        "UPDATE transactions SET sourceColor = 0xFFEF3124 WHERE source = 'Альфа'",
                    )
                } catch (e: Exception) {
                    CrashLoggerProvider.crashLogger.logDatabaseError("MIGRATION_9_10", "Ошибка миграции с 9 на 10", e)
                    throw e
                }
            }
        }

        /**
         * Миграция с версии 10 на версию 11
         * Устанавливает значение для sourceColor по умолчанию, чтобы использовался метод getEffectiveSourceColor()
         */
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Устанавливаем значение по умолчанию 0 для всех записей
                // Это заставит использовать логику метода getEffectiveSourceColor()
                db.execSQL("UPDATE transactions SET sourceColor = 0 WHERE sourceColor IS NULL")
            }
        }

        /**
         * Миграция с версии 11 на версию 12
         * Исправляет структуру таблицы transactions для решения проблемы с миграцией
         */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем новую таблицу с правильной структурой
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        id_string TEXT NOT NULL,
                        amount TEXT NOT NULL,
                        currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}',
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Наличные',
                        sourceColor INTEGER NOT NULL DEFAULT 0
                    )
                """,
                )

                // Копируем данные из старой таблицы в новую
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO transactions_new (id, id_string, amount, currencyCode, category, isExpense, date, note, source, sourceColor)
                    SELECT id, CAST(id AS TEXT), amount, '${Currency.RUB.code}', category, isExpense, date, note, source, sourceColor FROM transactions
                """,
                )

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE IF EXISTS transactions")

                // Переименовываем новую таблицу
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
            }
        }

        /**
         * Миграция с версии 12 на версию 13
         * Добавляет поле destination в таблицу transactions
         */
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем колонку destination с значением по умолчанию "Наличные"
                db.execSQL(
                    "ALTER TABLE transactions ADD COLUMN destination TEXT NOT NULL DEFAULT 'Наличные'",
                )
            }
        }

        /**
         * Миграция с версии 13 на версию 14
         * Добавляет поле isTransfer в таблицу transactions
         */
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the isTransfer column with a default value of 0 (false)
                db.execSQL(
                    "ALTER TABLE transactions ADD COLUMN isTransfer INTEGER NOT NULL DEFAULT 0",
                )
                Timber.i("Ran migration from version 13 to 14, adding isTransfer column.")
            }
        }

        /**
         * Миграция с версии 14 на версию 15
         * Исправляет структуру таблицы transactions для соответствия с TransactionEntity
         */
        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем временную таблицу с правильной схемой
                db.execSQL(
                    """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        id_string TEXT NOT NULL,
                        amount TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Наличные',
                        sourceColor INTEGER NOT NULL DEFAULT 0,
                        isTransfer INTEGER NOT NULL DEFAULT 0
                    )
                """,
                )

                // Копируем данные из старой таблицы в новую
                db.execSQL(
                    """
                    INSERT INTO transactions_temp (id, id_string, amount, category, isExpense, date, note, source, sourceColor, isTransfer)
                    SELECT id, id_string, amount, category, isExpense, date, note, source, sourceColor, isTransfer FROM transactions
                """,
                )

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE transactions")

                // Переименовываем временную таблицу
                db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")

                // Создаем индекс для id_string
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_id_string ON transactions (id_string)",
                )

                Timber.i(
                    "Выполнена миграция с версии 14 на 15, структура таблицы соответствует TransactionEntity",
                )
            }
        }

        // Миграция с 14 на 13 (для обратной совместимости при откате версии)
        private val MIGRATION_14_13 = object : Migration(14, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Пустая миграция, так как структура в версии 13 уже должна быть совместима
                // Здесь можно добавить обратные изменения, если в версии 14 были внесены изменения в схему
            }
        }

        // Миграция с 15 на 14 (для обратной совместимости при откате версии)
        private val MIGRATION_15_14 = object : Migration(15, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Пустая миграция, так как структура в версии 14 уже должна быть совместима
                Timber.i("Выполнена миграция с версии 15 на 14 (обратная совместимость)")
            }
        }

        /**
         * Миграция с версии 15 на версию 16
         * Добавляет поля walletIds, categoryId и title в таблицу transactions
         */
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем колонку wallet_ids (для хранения списка ID кошельков)
                db.execSQL("ALTER TABLE transactions ADD COLUMN wallet_ids TEXT DEFAULT NULL")

                // Добавляем колонку categoryId (для хранения ID категории/кошелька)
                db.execSQL(
                    "ALTER TABLE transactions ADD COLUMN categoryId TEXT NOT NULL DEFAULT ''",
                )

                // Добавляем колонку title (для хранения заголовка транзакции)
                db.execSQL("ALTER TABLE transactions ADD COLUMN title TEXT NOT NULL DEFAULT ''")

                Timber.i(
                    "Выполнена миграция с версии 15 на 16, добавлены поля wallet_ids, categoryId и title",
                )
            }
        }

        /**
         * Миграция с версии 16 на версию 15 (для обратной совместимости при откате версии)
         */
        private val MIGRATION_16_15 = object : Migration(16, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Обратная миграция не удаляет колонки, так как это может привести к потере данных
                // Просто обновляем версию схемы
                Timber.i("Выполнена миграция с версии 16 на 15 (обратная совместимость)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Получает экземпляр базы данных.
         * Создает базу данных, если она еще не создана.
         * @param context Контекст приложения
         * @return Экземпляр базы данных
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_13,
                        MIGRATION_14_15,
                        MIGRATION_15_14,
                        MIGRATION_15_16,
                        MIGRATION_16_15,
                    )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Создаем таблицу с правильной структурой при первом создании базы данных
                            db.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS transactions (
                                    id INTEGER PRIMARY KEY NOT NULL,
                                    id_string TEXT NOT NULL,
                                    amount TEXT NOT NULL,
                                    category TEXT NOT NULL,
                                    isExpense INTEGER NOT NULL,
                                    date INTEGER NOT NULL,
                                    note TEXT,
                                    source TEXT NOT NULL DEFAULT 'Наличные',
                                    sourceColor INTEGER NOT NULL DEFAULT 0,
                                    isTransfer INTEGER NOT NULL DEFAULT 0,
                                    categoryId TEXT NOT NULL DEFAULT '',
                                    title TEXT NOT NULL DEFAULT '',
                                    wallet_ids TEXT DEFAULT NULL
                                )
                            """,
                            )

                            // Создаем индекс для id_string
                            db.execSQL(
                                "CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_id_string ON transactions (id_string)",
                            )
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
