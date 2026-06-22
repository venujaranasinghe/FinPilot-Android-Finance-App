package com.bpeople.finpilot.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpeople.finpilot.data.database.dao.FreelanceProjectDao
import com.bpeople.finpilot.data.model.FreelanceProject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for FreelanceProjectDao operations.
 * Tests insert, update, delete, and query operations with status filters.
 */
@RunWith(AndroidJUnit4::class)
class FreelanceProjectDaoTest {

    private lateinit var database: FinPilotDatabase
    private lateinit var projectDao: FreelanceProjectDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinPilotDatabase::class.java).build()
        projectDao = database.freelanceProjectDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveProject() = runBlocking {
        val project = FreelanceProject(
            id = "project_1",
            userId = "user_1",
            clientName = "Tech Corp",
            projectTitle = "Web Development",
            agreedAmount = 500000.0,
            paidAmount = 250000.0,
            status = "IN_PROGRESS"
        )

        projectDao.insertProject(project)
        val retrieved = projectDao.getProjectByIdOnce("project_1")

        assert(retrieved != null)
        assert(retrieved?.clientName == "Tech Corp")
        assert(retrieved?.agreedAmount == 500000.0)
    }

    @Test
    fun insertMultipleAndQueryByStatus() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                paidAmount = 250000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                paidAmount = 0.0,
                status = "OPEN"
            ),
            FreelanceProject(
                id = "project_3",
                userId = "user_1",
                clientName = "Marketing Ltd",
                projectTitle = "Marketing Campaign",
                agreedAmount = 200000.0,
                paidAmount = 200000.0,
                status = "PAID"
            )
        )

        projectDao.insertProjectList(projects)
        val activeProjects = projectDao.getActiveProjects("user_1").first()

        assert(activeProjects.size == 2)
        assert(activeProjects.all { it.status in listOf("OPEN", "IN_PROGRESS") })
    }

    @Test
    fun queryByStatus() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                status = "COMPLETED"
            )
        )

        projectDao.insertProjectList(projects)
        val inProgressProjects = projectDao.getProjectsByStatus("user_1", "IN_PROGRESS").first()

        assert(inProgressProjects.size == 1)
        assert(inProgressProjects[0].clientName == "Tech Corp")
    }

    @Test
    fun updateProjectPayment() = runBlocking {
        val project = FreelanceProject(
            id = "project_1",
            userId = "user_1",
            clientName = "Tech Corp",
            projectTitle = "Web Development",
            agreedAmount = 500000.0,
            paidAmount = 250000.0,
            status = "IN_PROGRESS"
        )

        projectDao.insertProject(project)
        projectDao.updateProjectPayment("project_1", 400000.0)

        val retrieved = projectDao.getProjectByIdOnce("project_1")
        assert(retrieved?.paidAmount == 400000.0)
    }

    @Test
    fun updateProjectStatus() = runBlocking {
        val project = FreelanceProject(
            id = "project_1",
            userId = "user_1",
            clientName = "Tech Corp",
            projectTitle = "Web Development",
            agreedAmount = 500000.0,
            paidAmount = 500000.0,
            status = "COMPLETED"
        )

        projectDao.insertProject(project)
        projectDao.updateProjectStatus("project_1", "PAID")

        val retrieved = projectDao.getProjectByIdOnce("project_1")
        assert(retrieved?.status == "PAID")
    }

    @Test
    fun deleteProject() = runBlocking {
        val project = FreelanceProject(
            id = "project_1",
            userId = "user_1",
            clientName = "Tech Corp",
            projectTitle = "Web Development",
            agreedAmount = 500000.0,
            paidAmount = 250000.0,
            status = "IN_PROGRESS"
        )

        projectDao.insertProject(project)
        projectDao.deleteProject(project)

        val retrieved = projectDao.getProjectByIdOnce("project_1")
        assert(retrieved == null)
    }

    @Test
    fun deleteAllProjectsForUser() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                status = "OPEN"
            )
        )

        projectDao.insertProjectList(projects)
        projectDao.deleteAllProjectsForUser("user_1")

        val result = projectDao.getAllProjectsForUser("user_1").first()
        assert(result.isEmpty())
    }

    @Test
    fun getTotalAgreedAmount() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                status = "OPEN"
            )
        )

        projectDao.insertProjectList(projects)
        val total = projectDao.getTotalAgreedAmount("user_1")

        assert(total == 800000.0)
    }

    @Test
    fun getTotalOutstandingAmount() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                paidAmount = 250000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                paidAmount = 100000.0,
                status = "OPEN"
            )
        )

        projectDao.insertProjectList(projects)
        val outstanding = projectDao.getTotalOutstandingAmount("user_1")

        assert(outstanding == 450000.0)
    }

    @Test
    fun getUnpaidProjects() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                paidAmount = 250000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                paidAmount = 300000.0,
                status = "PAID"
            )
        )

        projectDao.insertProjectList(projects)
        val unpaidProjects = projectDao.getUnpaidProjects("user_1").first()

        assert(unpaidProjects.size == 1)
        assert(unpaidProjects[0].clientName == "Tech Corp")
    }

    @Test
    fun getActiveProjectCount() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                status = "OPEN"
            ),
            FreelanceProject(
                id = "project_3",
                userId = "user_1",
                clientName = "Marketing Ltd",
                projectTitle = "Campaign",
                agreedAmount = 200000.0,
                status = "COMPLETED"
            )
        )

        projectDao.insertProjectList(projects)
        val count = projectDao.getActiveProjectCount("user_1")

        assert(count == 2)
    }

    @Test
    fun getAllClients() = runBlocking {
        val projects = listOf(
            FreelanceProject(
                id = "project_1",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "Web Development",
                agreedAmount = 500000.0,
                status = "IN_PROGRESS"
            ),
            FreelanceProject(
                id = "project_2",
                userId = "user_1",
                clientName = "Design Inc",
                projectTitle = "UI Design",
                agreedAmount = 300000.0,
                status = "OPEN"
            ),
            FreelanceProject(
                id = "project_3",
                userId = "user_1",
                clientName = "Tech Corp",
                projectTitle = "App Development",
                agreedAmount = 800000.0,
                status = "COMPLETED"
            )
        )

        projectDao.insertProjectList(projects)
        val clients = projectDao.getAllClients("user_1").first()

        assert(clients.size == 2)
        assert(clients.contains("Tech Corp"))
        assert(clients.contains("Design Inc"))
    }
}
