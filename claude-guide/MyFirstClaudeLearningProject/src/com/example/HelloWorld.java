package com.example;

/**
 * HelloWorld - 输出当前使用的 AI 模型信息
 * 这是 Claude Code 学习的第一个 Java 项目
 */
public class HelloWorld {

    // 当前使用的 AI 模型
    private static final String AI_MODEL = "Claude Code (Powered by Claude 4.6 / qwen3.5-plus)";
    private static final String PROJECT_NAME = "MyFirstClaudeLearningProject";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  欢迎使用 " + PROJECT_NAME);
        System.out.println("========================================");
        System.out.println();
        System.out.println("当前使用的 AI 模型：" + AI_MODEL);
        System.out.println("项目类型：Java 开发项目");
        System.out.println("创建时间：2026-03-06");
        System.out.println();
        System.out.println("Hello, World!");
        System.out.println("========================================");
    }
}