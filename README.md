# OSSD26-Lab2-2024112758

哈尔滨工业大学《开源软件开发实践》Lab 2：开源软件开发协作流程 —— 学号 2024112758

本仓库对应**实验内容 2**：新建仓库，并把**实验内容 1** 中建立的代码仓库（`Qtmenxia/OOSP-Lab2-2026`）
的全部内容（包含代码修改成功后的文件）复制过来，用于与另一位同学相互 Fork、评审并接受 Pull Request。

## 一、仓库内容

| 文件 | 说明 |
|---|---|
| `README.md` | 本说明文件（原仓库的 `# OSSDP-Lab2` 说明已扩写为完整说明） |
| `Solution1.java` ~ `Solution20.java` | 实验内容 1 的 20 道编程题目原始实现；其中 **`Solution3.java` 是已修复的第 3 题** |
| `L2024112758_3_Test.java` | 第 3 题（最大整除子集）的 JUnit 5 单元测试类，23 个用例，全部通过 |

## 二、第 3 题：最大整除子集

给定一个由**无重复正整数**组成的集合 `nums`，找出其中最大的整除子集：子集中任意两个元素
`(answer[i], answer[j])` 都满足 `answer[i] % answer[j] == 0` 或 `answer[j] % answer[i] == 0`；
若存在多个有效解，返回任意一个即可。约束：`1 <= nums.length <= 1000`，`1 <= nums[i] <= 2 * 10^9`。

原始实现存在 6 处缺陷：

| 编号 | 原始代码 | 问题 | 修复 |
|---|---|---|---|
| Bug 1 | `int len = nums.length - 1;` | 少算 1，最大元素永不参与计算；`n = 1` 越界、`n = 0` 负长度数组 | `int len = nums.length;` |
| Bug 2 | `int maxVal = dp[0];` | 初始化语义错误，应记录元素值而非 `dp[0]` | `int maxVal = nums[0];` |
| Bug 3 | `for (int j = 1; j < i; j++)` | 漏掉 `j = 0`，最小元素无法作为前驱 | `for (int j = 0; j < i; j++)` |
| Bug 4 | `maxVal = i;` | 记录下标而非元素值，倒推时整除判断失真 | `maxVal = nums[i];` |
| Bug 5 | 缺少空数组保护 | `NegativeArraySizeException` / `ArrayIndexOutOfBoundsException` | 入口判空返回空列表 |
| Bug 6 | 缺少 `null` 保护 | 直接抛 `NullPointerException` | 抛出 `IllegalArgumentException` |

实验内容 1 的 Pull Request：<https://github.com/HanchuanXu/OOSP-Lab2-2026/pull/13>

## 三、如何运行第 3 题的单元测试

```bash
# 1. 获取 JUnit 5 控制台启动器（也可以直接在 IDE 中使用 JUnit 5）
curl -L -o junit-platform-console-standalone-1.11.4.jar \
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar

# 2. 只编译第 3 题与被测类（其余 Solution 文件中有多个同名 Solution 类，不能一起编译）
javac -encoding UTF-8 -d out -cp junit-platform-console-standalone-1.11.4.jar \
  Solution3.java L2024112758_3_Test.java

# 3. 执行
java -jar junit-platform-console-standalone-1.11.4.jar execute \
  --class-path out --select-class L2024112758_3_Test --details=tree
```

预期输出：`23 tests successful`、`0 tests failed`（同一套用例在修复前的原始代码上有 21 个用例失败）。

## 四、测试设计要点

- 五类用例：正常输入、边界值、非法/异常输入、特殊输入、回归测试（每个 Bug 至少一个回归用例）；
- 由于答案不唯一，断言「合法性（元素互不相同、全部来自输入、两两整除）+ 最优性（长度等于独立暴力枚举基准）」；
- `n <= 16` 时用位掩码暴力枚举给出与被测动态规划完全独立的最优长度基准，避免「用同一个算法验证自己」。
