package com.github.rishabhdeepsingh.jhelper20.parser

import com.github.rishabhdeepsingh.jhelper20.task.Task
import com.github.rishabhdeepsingh.jhelper20.task.Test as TaskTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CompetitiveCompanionTest {

    @Test
    fun `test parseJson with valid JSON and single test case`() {
        val parser = CompetitiveCompanion()
        val sampleJson =
            """{"name":"A. Submission is All You Need","group":"Codeforces - Codeforces Round 1040 (Div. 2)","url":"https://codeforces.com/contest/2130/problem/A","interactive":false,"memoryLimit":256,"timeLimit":1000,"tests":[{"input":"2\n3\n0 1 1\n3\n1 2 3\n","output":"3\n6\n"}],"testType":"single","input":{"type":"stdin"},"output":{"type":"stdout"},"languages":{"java":{"mainClass":"Main","taskClass":"ASubmissionIsAllYouNeed"}},"batch":{"id":"64c16d9b-7dfd-4e35-a137-48542a6cfdf0","size":1}}"""

        val result = parser.parseJsonTask(sampleJson)!!

        assertEquals(
            result, Task(
                name = "A. Submission is All You Need",
                contestName = "Codeforces - Codeforces Round 1040 (Div. 2)",
                tests = listOf(
                    TaskTest(
                        input = """
                        2
                        3
                        0 1 1
                        3
                        1 2 3
                    """.trimIndent(), output = """
                        3
                        6
                    """.trimIndent(), index = 0, active = true
                    )
                )
            )
        )
    }
}