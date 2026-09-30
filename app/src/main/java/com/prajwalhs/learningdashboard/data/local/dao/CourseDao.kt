package com.prajwalhs.learningdashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.prajwalhs.learningdashboard.data.local.entity.CourseEntity
import com.prajwalhs.learningdashboard.data.local.entity.LessonEntity
import com.prajwalhs.learningdashboard.data.local.projection.CourseProgressProjection
import com.prajwalhs.learningdashboard.data.local.relation.CourseWithLessons
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // ---------- Reads (observed by the UI; Room re-emits on every table change) ----------

    @Query(
        """
        SELECT c.id AS id,
               c.title AS title,
               c.instructor AS instructor,
               COUNT(l.id) AS total_lessons,
               COALESCE(SUM(l.is_completed), 0) AS completed_lessons
        FROM courses AS c
        LEFT JOIN lessons AS l ON l.course_id = c.id
        GROUP BY c.id
        ORDER BY c.id
        """
    )
    fun observeCourseProgress(): Flow<List<CourseProgressProjection>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourseWithLessons(courseId: Int): Flow<CourseWithLessons?>

    @Query("SELECT id FROM lessons WHERE is_completed = 1")
    suspend fun getCompletedLessonIds(): List<Int>

    // ---------- Writes ----------

    // @Upsert updates in place. REPLACE would delete + reinsert, and the CASCADE
    // foreign key would wipe the course's lessons along with local progress.
    @Upsert
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    suspend fun deleteCoursesNotIn(keepIds: List<Int>)

    @Query("DELETE FROM lessons WHERE id NOT IN (:keepIds)")
    suspend fun deleteLessonsNotIn(keepIds: List<Int>)

    @Query("UPDATE lessons SET is_completed = 1 WHERE id = :lessonId")
    suspend fun markLessonCompleted(lessonId: Int)

    /**
     * Replaces the cache with the latest server data in a single transaction.
     *
     * Conflict rule: local completion wins. A lesson completed on this device stays
     * completed even if the server still reports it as pending (the server has not
     * received that update yet). A production app would queue those changes and
     * sync them to the server.
     */
    @Transaction
    suspend fun syncWithRemote(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        val locallyCompletedIds = getCompletedLessonIds().toSet()
        val mergedLessons = lessons.map { lesson ->
            if (lesson.id in locallyCompletedIds) lesson.copy(isCompleted = true) else lesson
        }

        deleteCoursesNotIn(courses.map { it.id })   // CASCADE removes their lessons
        upsertCourses(courses)
        deleteLessonsNotIn(mergedLessons.map { it.id })
        upsertLessons(mergedLessons)
    }
}