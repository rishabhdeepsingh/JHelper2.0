package com.github.rishabhdeepsingh.jhelper20.generation

import com.github.rishabhdeepsingh.jhelper20.common.CommonUtils.generatePSIFromTask
import com.github.rishabhdeepsingh.jhelper20.common.firstRootSource
import com.github.rishabhdeepsingh.jhelper20.states.ProjectConfigurationState
import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.github.rishabhdeepsingh.jhelper20.generation.TemplatesUtils.getTemplate
import com.github.rishabhdeepsingh.jhelper20.task.StreamType
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.github.rishabhdeepsingh.jhelper20.task.TestType
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

object CodeGenerationUtils {
    /**
     * Generates the main function for testing purposes.
     *
     * @param project Project to get configuration from
     */
    fun generateRunFile(project: Project, inputFile: PsiFile, task: TaskConfiguration) {
        if (FileUtils.isNotCppFile(inputFile)) {
            throw NotificationException("Not a cpp file", "Only cpp files are currently supported")
        }

        val psiOutputFile = getRunFile(project)

        FileUtils.writeToFile(
            psiOutputFile, generateRunFileContent(
                project,
                task,
                FileUtil.getRelativePath(psiOutputFile.virtualFile.parent.path, inputFile.virtualFile.path, '/')
            )
        )
    }

    private fun generateRunFileContent(project: Project, task: TaskConfiguration, path: String?): String =
        getTemplate(project, "run").replaceAll(TemplatesUtils.TASK_FILE, path)
            .replaceAll(TemplatesUtils.TESTS, generateTestDeclaration(task.tests))
            .replaceAll(TemplatesUtils.CLASS_NAME, task.className)
            .replaceAll(TemplatesUtils.SOLVER_CALL, generateSolverCall(task.testType))

    private fun generateTestDeclaration(tests: List<Test>): String = tests.joinToString { test ->
        """
            |{
            | ${quote(test.input)}, ${quote(test.output)},
            | ${test.active}, ${true}
            |}
            """.trimMargin()
    }

    private fun quote(input: String): CharSequence {
        val sb = StringBuilder()
        sb.append('"')
        for (c in input.toCharArray()) {
            if (c == '\n') {
                sb.append("\\n")
                continue
            }
            if (c == '"' || c == '\'' || c == '\\') {
                sb.append('\\')
            }
            sb.append(c)
        }
        sb.append('"')
        return sb
    }

    private fun generateSolverCall(testType: TestType): String = when (testType) {
        TestType.SINGLE -> "solver.solve();"
        TestType.MULTI_NUMBER -> """
                int n;
                cin >> n;
                for (int i = 0; i < n; ++i) {
                  solver.solve();
                }
                """.trimIndent()

        TestType.MULTI_EOF -> """
                while (cin.good())
                  solver.solve(cin,cout);
                }
               """.trimIndent()
    }

    private fun getRunFile(project: Project): PsiFile {
        val configuration = ProjectConfigurationState.getInstance()

        val outputFile =
            project.firstRootSource().findFileByRelativePath(configuration.runFile) ?: throw NotificationException(
                "No run file found.", "You should configure run file to point to existing file"
            )

        return PsiManager.getInstance(project).findFile(outputFile)
            ?: throw NotificationException("Couldn't open run file as PSI")
    }

    /**
     * Generates code for submission.
     * Adds the main function, inlines all used code except the standard library and puts it to an output file from configuration
     *
     * @param project Project to get configuration from
     */
    fun generateSubmissionFileForTask(project: Project, taskConfiguration: TaskConfiguration) {
        generateSubmissionFile(project, generatePSIFromTask(project, taskConfiguration), taskConfiguration)
    }

    private fun generateSubmissionFile(project: Project, inputFile: PsiFile, task: TaskConfiguration) {
        if (FileUtils.isNotCppFile(inputFile)) {
            throw NotificationException("Not a cpp file", "Only cpp files are currently supported")
        }
        val result = IncludesProcessor.process(inputFile)
        val psiOutputFile = getOutputFile(project)

        FileUtils.writeToFile(
            psiOutputFile, generateSubmissionFileContent(project, result, task)
        )
        if (ProjectConfigurationState.getInstance().isCodeReformattingOn) {
            ReformatCodeProcessor(psiOutputFile, false).run()
        }
    }

    private fun generateSubmissionFileContent(project: Project, code: String, task: TaskConfiguration): String {
        var updatedCode = code
        val template = getTemplate(project, "submission")
        if (task.input.type == StreamType.LOCAL_REGEXP) {
            updatedCode = "$code\n${generateFileNameGetter()}"
        }
        return template.replaceAll(TemplatesUtils.CODE, updatedCode)
            .replaceAll(TemplatesUtils.CLASS_NAME, (task.className))
            .replaceAll(TemplatesUtils.INPUT, getInputDeclaration(task))
            .replaceAll(TemplatesUtils.OUTPUT, getOutputDeclaration(task))
            .replaceAll(TemplatesUtils.SOLVER_CALL, generateSolverCall(task.testType))
    }

    private fun generateFileNameGetter(): String {
        return """
                |#include <dirent.h>
                |#include <stdexcept>
                |#include <regex>
                |#include <sys/stat.h>
                |#include <cstdint>
                |
                |std::string getLastFileName(const std::string& regexString) {
                |  DIR* dir;
                |  dirent* entry;
                |  std::string result = "";
                |  int64_t resultModificationTime = 0;
                |  std::regex regex(regexString);
                |  if ((dir = opendir (".")) != NULL) {
                |    while ((entry = readdir (dir)) != NULL) {
                |      if (std::regex_match(entry->d_name, regex)) {
                |        struct stat buffer;
                |        stat(entry->d_name, &buffer);
                |        int64_t modificationTime = static_cast<int64_t>(buffer.st_mtimespec.tv_sec) * 1000000000 +
                |            static_cast<int64_t>(buffer.st_mtimespec.tv_nsec);
                |
                |        if (modificationTime > resultModificationTime) {
                |          resultModificationTime = modificationTime;
                |          result = entry->d_name;
                |        }
                |      }
                |    }
                |    closedir (dir);
                |  } else {
                |    throw std::runtime_error("Couldn't open current directory");
                |  }
                |  if (result.empty()) {
                |    throw std::runtime_error("No file found");
                |  }  
                |  return result;
                |}
                """.trimMargin()
    }

    private fun getOutputDeclaration(task: TaskConfiguration): String {
        val outputFileName = task.output.getFileName(task.name, ".out")
        if (outputFileName == null) {
            return ""
        } else if (task.output.type == StreamType.LOCAL_REGEXP) {
            return """freopen("$outputFileName", "w", stdout);"""
        }
        throw NotificationException("Your task is in inconsistent state", "Can't output to local regexp")
    }

    private fun getInputDeclaration(task: TaskConfiguration): String = when (task.input.type) {
        StreamType.LOCAL_REGEXP -> """freopen(getLastFileName(${quote(task.input.fileName!!)}).c_str(), "r", stdin);"""
        StreamType.STANDARD -> ""
        else -> """freopen("${task.input.getFileName(task.name, ".in")}", "r", stdin);"""
    }

    private fun getOutputFile(project: Project): PsiFile {
        val outputFile =
            project.firstRootSource().findFileByRelativePath(ProjectConfigurationState.getInstance().outputFile)
                ?: throw NotificationException(
                    "No output file found.",
                    "You should configure output file to point to existing file"
                )
        return PsiManager.getInstance(project).findFile(outputFile)
            ?: throw NotificationException("Couldn't open output file as PSI")
    }
}