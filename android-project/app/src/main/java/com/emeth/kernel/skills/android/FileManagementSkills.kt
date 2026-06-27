package com.emeth.kernel.skills.android

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import java.util.Locale
import kotlin.math.max

data class SmartFileCandidate(
    val displayName: String,
    val uri: String,
    val mimeType: String?,
    val relativePath: String?,
    val sizeBytes: Long,
    val modifiedAtSeconds: Long,
    val score: Int
)

data class DuplicateFileGroup(
    val key: String,
    val totalBytes: Long,
    val files: List<SmartFileCandidate>
)

class FileManagementSkill(private val context: Context) : Skill {
    override val id = "android.files.smart"
    override val name = "Smart File Management"
    override val description = "Finds, ranks, and prepares safe file management actions through Android file intents"

    override fun canHandle(intent: Intent): Boolean {
        return intent in setOf(
            Intent.FIND_FILE,
            Intent.DELETE_FILE,
            Intent.MOVE_FILE,
            Intent.FIND_DUPLICATE_FILES
        )
    }

    override fun execute(request: SkillRequest): SkillResult {
        return when (request.command.intentType) {
            Intent.FIND_FILE -> findFiles(request)
            Intent.DELETE_FILE -> prepareDelete(request)
            Intent.MOVE_FILE -> prepareMove(request)
            Intent.FIND_DUPLICATE_FILES -> findDuplicates()
            else -> SkillResult.Failure("Unsupported file intent")
        }
    }

    private fun findFiles(request: SkillRequest): SkillResult {
        val query = request.command.fileQuery ?: request.command.query ?: request.command.rawText
        val matches = searchFiles(query, limit = 20)
        if (matches.isEmpty()) {
            return SkillResult.Partial("I couldn't find file matches for \"$query\". Try a file name, extension, or folder hint.")
        }

        return SkillResult.Success(
            "Found ${matches.size} smart file match${if (matches.size == 1) "" else "es"} for \"$query\".",
            matches
        )
    }

    private fun prepareDelete(request: SkillRequest): SkillResult {
        val query = request.command.fileQuery ?: request.command.query ?: return SkillResult.Partial("Which file should I find for deletion?")
        val matches = searchFiles(query, limit = 20)
        if (matches.isEmpty()) {
            return SkillResult.Partial("I couldn't find files matching \"$query\" to delete.")
        }

        return SkillResult.Partial(
            "I found ${matches.size} possible file${if (matches.size == 1) "" else "s"} for deletion. Pick the exact item before I request Android's delete confirmation.",
            matches
        )
    }

    private fun prepareMove(request: SkillRequest): SkillResult {
        val query = request.command.fileQuery ?: request.command.query ?: return SkillResult.Partial("Which file should I move?")
        val matches = searchFiles(query, limit = 20)
        if (matches.isEmpty()) {
            return SkillResult.Partial("I couldn't find files matching \"$query\" to move.")
        }

        return SkillResult.Partial(
            "I found ${matches.size} possible file${if (matches.size == 1) "" else "s"} to move. Pick one, then choose the destination folder.",
            matches
        )
    }

    private fun findDuplicates(): SkillResult {
        val files = queryRecentFiles(limit = 500)
        val duplicateGroups = files
            .filter { it.sizeBytes > 0 }
            .groupBy { "${it.displayName.lowercase(Locale.ROOT)}:${it.sizeBytes}" }
            .mapNotNull { (key, group) ->
                if (group.size > 1) {
                    DuplicateFileGroup(
                        key = key,
                        totalBytes = group.drop(1).sumOf { it.sizeBytes },
                        files = group.sortedByDescending { it.modifiedAtSeconds }
                    )
                } else {
                    null
                }
            }
            .sortedByDescending { it.totalBytes }
            .take(20)

        if (duplicateGroups.isEmpty()) {
            return SkillResult.Success("I did not find obvious duplicate files by same name and size.")
        }

        return SkillResult.Partial(
            "I found ${duplicateGroups.size} duplicate group${if (duplicateGroups.size == 1) "" else "s"}. Review them before deleting anything.",
            duplicateGroups
        )
    }

    private fun searchFiles(query: String, limit: Int): List<SmartFileCandidate> {
        val tokens = query
            .lowercase(Locale.ROOT)
            .split(Regex("\\s+"))
            .filter { it.length > 1 && it !in ignoredQueryWords }

        if (tokens.isEmpty()) return emptyList()

        return queryRecentFiles(limit = 500)
            .map { candidate -> candidate.copy(score = score(candidate, tokens)) }
            .filter { it.score > 0 }
            .sortedWith(compareByDescending<SmartFileCandidate> { it.score }.thenByDescending { it.modifiedAtSeconds })
            .take(limit)
    }

    private fun score(candidate: SmartFileCandidate, tokens: List<String>): Int {
        val name = candidate.displayName.lowercase(Locale.ROOT)
        val path = candidate.relativePath?.lowercase(Locale.ROOT).orEmpty()
        val compactName = name.replace(Regex("[^a-z0-9]"), "")
        val compactQuery = tokens.joinToString("").replace(Regex("[^a-z0-9]"), "")

        var score = 0
        if (compactQuery.isNotBlank() && compactName.contains(compactQuery)) score += 60
        for (token in tokens) {
            if (name == token) score += 40
            if (name.contains(token)) score += 25
            if (path.contains(token)) score += 10
        }
        if (tokens.all { name.contains(it) || path.contains(it) }) score += 30
        if (name.contains("latest") || path.contains("download")) score += 5
        return max(score, 0)
    }

    private fun queryRecentFiles(limit: Int): List<SmartFileCandidate> {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = buildList {
            add(MediaStore.Files.FileColumns._ID)
            add(MediaStore.Files.FileColumns.DISPLAY_NAME)
            add(MediaStore.Files.FileColumns.SIZE)
            add(MediaStore.Files.FileColumns.DATE_MODIFIED)
            add(MediaStore.Files.FileColumns.MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Files.FileColumns.RELATIVE_PATH)
            }
        }.toTypedArray()

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        val result = mutableListOf<SmartFileCandidate>()

        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val modifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val pathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
            } else {
                -1
            }

            while (cursor.moveToNext() && result.size < limit) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: continue
                val uri: Uri = ContentUris.withAppendedId(collection, id)
                result += SmartFileCandidate(
                    displayName = name,
                    uri = uri.toString(),
                    mimeType = cursor.getString(mimeIndex),
                    relativePath = if (pathIndex >= 0) cursor.getString(pathIndex) else null,
                    sizeBytes = cursor.getLong(sizeIndex),
                    modifiedAtSeconds = cursor.getLong(modifiedIndex),
                    score = 0
                )
            }
        }

        return result
    }

    private companion object {
        val ignoredQueryWords = setOf(
            "file",
            "files",
            "latest",
            "downloaded",
            "download",
            "delete",
            "remove",
            "move",
            "find",
            "fetch",
            "search",
            "locate"
        )
    }
}
