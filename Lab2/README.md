# Lab 2：开源软件开发协作流程 —— 第 3 题（最大整除子集）

本目录保存 Lab 2 实验内容 1 中分配的**第 3 题**代码与单元测试。

| 项目 | 内容 |
|---|---|
| 学号 | 2024112758 |
| 题目编号 | 3 |
| 题目名称 | 最大整除子集（Largest Divisible Subset） |
| 被测源文件 | `Solution3.java` |
| 测试类 | `L2024112758_3_Test.java` |
| 测试框架 | JUnit 5（junit-platform-console-standalone 1.11.4） |
| JDK | 21 |

## 一、题目说明

给定一个由**无重复正整数**组成的集合 `nums`，找出其中最大的整除子集：子集中任意两个元素
`(answer[i], answer[j])` 都满足 `answer[i] % answer[j] == 0` 或 `answer[j] % answer[i] == 0`。
若存在多个有效解，返回任意一个即可。

约束：`1 <= nums.length <= 1000`，`1 <= nums[i] <= 2 * 10^9`，元素互不相同。

## 二、修复的缺陷

原始实现（`HanchuanXu/OOSP-Lab2-2026` 的 `Solution3.java`）存在 6 处缺陷，详见 PR：
https://github.com/HanchuanXu/OOSP-Lab2-2026/pull/13

| 编号 | 原始代码 | 问题 | 修复 |
|---|---|---|---|
| Bug 1 | `int len = nums.length - 1;` | 少算 1，最大元素永不参与计算；`n = 1` 时 `len = 0` 越界，`n = 0` 时负长度数组 | `int len = nums.length;` |
| Bug 2 | `int maxVal = dp[0];` | 初始化语义错误，应记录元素值而非 `dp[0]` | `int maxVal = nums[0];` |
| Bug 3 | `for (int j = 1; j < i; j++)` | 漏掉 `j = 0`，最小元素无法作为前驱 | `for (int j = 0; j < i; j++)` |
| Bug 4 | `maxVal = i;` | 记录下标而非元素值，倒推时整除判断失真 | `maxVal = nums[i];` |
| Bug 5 | 缺少空数组/短数组保护 | `NegativeArraySizeException` / `ArrayIndexOutOfBoundsException` | 入口判空返回空列表 |
| Bug 6 | 缺少 `null` 保护 | 直接抛 `NullPointerException` | 抛出 `IllegalArgumentException` |

## 三、如何运行测试

```bash
# 1. 下载 JUnit 5 控制台启动器（或使用任意 IDE 自带的 JUnit 5）
curl -L -o junit-platform-console-standalone-1.11.4.jar \
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar

# 2. 编译
javac -encoding UTF-8 -d out -cp junit-platform-console-standalone-1.11.4.jar \
  Solution3.java L2024112758_3_Test.java

# 3. 执行
java -jar junit-platform-console-standalone-1.11.4.jar execute \
  --class-path out --select-class L2024112758_3_Test --details=tree
```

实测结果：**23 个测试用例全部通过**（同一套用例在修复前的原始代码上有 21 个失败）。

## 四、测试设计要点

- 等价类划分、边界值分析、异常输入、特殊输入、回归测试五类用例；
- 由于答案不唯一，断言「合法性（元素互不相同、全部来自输入、两两整除）+ 最优性（长度等于独立暴力枚举基准）」；
- `n <= 16` 时用位掩码暴力枚举给出与被测算法完全独立的最优长度基准；
- 每个修复的 Bug 至少对应一个专用回归用例。
