package com.example.crossstitch.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATION_1_2:Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `GameProgress` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `patternId` INTEGER NOT NULL,
                `myCrossStitchGrid` TEXT NOT NULL,
                `completedCells` INTEGER NOT NULL,
                `mistake` INTEGER NOT NULL,
                FOREIGN KEY(`patternId`) REFERENCES `Pattern`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX `index_GameProgress_patternId` ON `GameProgress` (`patternId`)")
    }
}