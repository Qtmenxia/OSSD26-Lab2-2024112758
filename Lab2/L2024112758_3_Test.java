import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第 3 题（最大整除子集）单元测试。
 *
 * <p><b>学号：2024112758　　题目编号：3　　被测类：{@link Solution3}</b></p>
 *
 * <h2>一、测试用例设计的总体原则</h2>
 * <ol>
 *   <li><b>等价类划分</b>：把输入划分为「存在可整除关系（最优解长度 &gt; 1）」与
 *       「不存在任何可整除关系（最优解长度 = 1）」两个有效等价类；把输入划分为
 *       「null」「空数组」等无效等价类。每个等价类至少取一个用例，避免重复用例。</li>
 *   <li><b>边界值分析</b>：围绕题目约束 1 &lt;= nums.length &lt;= 1000、1 &lt;= nums[i] &lt;= 2*10^9 取
 *       最小长度 1、最大长度 1000、最小取值 1、最大取值 2*10^9，以及长度为 2 的最小多点情形。</li>
 *   <li><b>异常输入测试</b>：对 null 要求抛出 IllegalArgumentException（而不是 NullPointerException），
 *       对空数组要求返回空列表且不抛任何异常。</li>
 *   <li><b>特殊输入测试</b>：无序输入、全为 2 的幂的降序输入、「1 + 若干质数」这类只有 1 能连接其他
 *       元素的输入、以及存在多个长度相同的最优解（答案不唯一）的输入。</li>
 *   <li><b>最优性与合法性双重断言</b>：由于答案不唯一，不能只比较字面结果。每个用例都断言：
 *       (a) 返回值非 null；(b) 元素互不相同且都来自输入；(c) 结果内部两两整除；
 *       (d) 结果长度等于「最大整除子集长度」的独立基准值。基准值 n &lt;= 16 时由
 *       {@link #bruteForceMaxSize(int[])} 位掩码暴力枚举给出（与被测的动态规划算法完全独立，
 *       避免「用同一个算法验证自己」），n &gt; 16 时用可人工推导的确定值。</li>
 *   <li><b>回归测试</b>：针对本次修复的每一个 Bug 各设一个回归用例（见文末 testRegressionXxx），
 *       保证修复后不会再次退化。这些用例在修复前的原始代码上是失败的（详见实验报告 2.5 节）。</li>
 * </ol>
 *
 * <p>说明：题目保证 nums 中元素互不相同且均为正整数，因此不设计「重复值」「负数」「0」用例，
 * 这些输入不属于本题的输入域，其行为未在题目中定义。</p>
 */
public class L2024112758_3_Test {

    /** 被测对象，每个测试方法都使用独立的实例，避免测试之间通过对象状态相互影响。 */
    private final Solution3 solution = new Solution3();

    // ------------------------------------------------------------------
    // 断言辅助方法
    // ------------------------------------------------------------------

    /**
     * 断言返回值是一个「合法的整除子集」：非 null、元素互不相同、全部来自输入、且两两整除。
     *
     * @param nums   输入数组（方法内部会复制，不对入参做任何修改）
     * @param result 被测方法的返回值
     */
    private void assertValidDivisibleSubset(int[] nums, List<Integer> result) {
        assertNotNull(result, "返回值不应为 null");
        assertTrue(!result.isEmpty(), "输入非空时返回值不应为空");

        Set<Integer> input = new HashSet<Integer>();
        for (int v : nums) {
            input.add(v);
        }
        Set<Integer> seen = new HashSet<Integer>();
        for (Integer v : result) {
            assertNotNull(v, "返回结果中不应包含 null 元素");
            assertTrue(input.contains(v), "返回元素 " + v + " 不在输入集合中");
            assertTrue(seen.add(v), "返回结果中出现重复元素 " + v);
        }

        // 升序后只需检查相邻元素即可（整除关系具有传递性：a|b 且 b|c 蕴含 a|c）
        List<Integer> sorted = new ArrayList<Integer>(result);
        sorted.sort(null);
        for (int i = 1; i < sorted.size(); i++) {
            assertEquals(0, sorted.get(i) % sorted.get(i - 1),
                    "结果中 " + sorted.get(i - 1) + " 与 " + sorted.get(i) + " 不满足整除关系");
        }
    }

    /**
     * 断言返回值既合法又是最优的。
     *
     * @param nums     输入
     * @param result   返回值
     * @param expected 期望的最大子集长度（n &lt;= 16 时应传入暴力枚举基准值）
     */
    private void assertValidAndOptimal(int[] nums, List<Integer> result, int expected) {
        assertValidDivisibleSubset(nums, result);
        assertEquals(expected, result.size(),
                "子集长度不是最优，返回 = " + result + "，期望长度 = " + expected);
    }

    /**
     * 独立的暴力枚举基准实现：枚举 nums 的全部非空子集，找出最大的整除子集长度。
     * 仅用于 n &lt;= 16（2^16 = 65536 个子集）的用例，作为与被测动态规划算法无关的正确性基准。
     *
     * @param nums 输入数组，长度 &lt;= 16
     * @return 最大整除子集的长度
     */
    private int bruteForceMaxSize(int[] nums) {
        int n = nums.length;
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        int best = 0;
        for (int mask = 1; mask < (1 << n); mask++) {
            int prev = -1;
            int count = 0;
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    if (prev != -1 && sorted[i] % prev != 0) {
                        ok = false;
                        break;
                    }
                    prev = sorted[i];
                    count++;
                }
            }
            if (ok && count > best) {
                best = count;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // 一、正常输入：覆盖主要功能（等价类「存在整除关系」）
    // ------------------------------------------------------------------

    /**
     * 测试目的：验证题目示例 1 的基本功能，并确认「答案不唯一」时返回任一最优解均可。
     * 测试用例：nums = [1,2,3]。
     * 预期结果：返回长度为 2 的合法整除子集，[1,2] 与 [1,3] 都算正确。
     */
    @Test
    @DisplayName("正常输入-示例1 [1,2,3]")
    public void testExample1() {
        int[] nums = {1, 2, 3};
        List<Integer> result = solution.largestDivisibleSubset(nums);
        assertValidAndOptimal(new int[]{1, 2, 3}, result, bruteForceMaxSize(new int[]{1, 2, 3}));
        assertEquals(2, result.size());
    }

    /**
     * 测试目的：验证题目示例 2，最优解唯一且为全部元素时的正确性。
     * 测试用例：nums = [1,2,4,8]（2 的幂序列，任意两元素均整除）。
     * 预期结果：返回 [1,2,4,8]（顺序不限），长度为 4。
     */
    @Test
    @DisplayName("正常输入-示例2 [1,2,4,8]")
    public void testExample2() {
        int[] nums = {1, 2, 4, 8};
        List<Integer> result = solution.largestDivisibleSubset(nums);
        assertValidAndOptimal(new int[]{1, 2, 4, 8}, result, 4);
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 4, 8)), new HashSet<Integer>(result));
    }

    /**
     * 测试目的：覆盖等价类「输入中不存在任何可整除关系」，验证退化为单元素子集的处理分支。
     * 测试用例：nums = [2,3,5]（均为质数且互不整除）。
     * 预期结果：返回长度为 1 的列表（任一元素均可）。
     */
    @Test
    @DisplayName("正常输入-无任何整除关系 [2,3,5]")
    public void testNoDivisiblePair() {
        int[] nums = {2, 3, 5};
        List<Integer> result = solution.largestDivisibleSubset(nums);
        assertValidAndOptimal(new int[]{2, 3, 5}, result, 1);
    }

    /**
     * 测试目的：验证算法对无序输入的处理（内部排序是否正确）。
     * 测试用例：nums = [8,1,4,2]（与示例 2 元素相同但顺序打乱）。
     * 预期结果：返回 [1,2,4,8] 的某种顺序，长度为 4。
     */
    @Test
    @DisplayName("正常输入-无序输入 [8,1,4,2]")
    public void testUnsortedInput() {
        int[] nums = {8, 1, 4, 2};
        List<Integer> result = solution.largestDivisibleSubset(nums);
        assertValidAndOptimal(new int[]{8, 1, 4, 2}, result, 4);
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 4, 8)), new HashSet<Integer>(result));
    }

    /**
     * 测试目的：验证存在多个长度相同的最优解时，返回的仍是其中一个合法的最优解（不出现「拼接」错误）。
     * 测试用例：nums = [1,2,3,4,6,12]，最优解长度 4（如 1-2-4-12、1-3-6-12、1-2-6-12）。
     * 预期结果：返回长度为 4（= 暴力枚举基准）的合法整除子集。
     */
    @Test
    @DisplayName("正常输入-多最优解 [1,2,3,4,6,12]")
    public void testMultipleOptimalSolutions() {
        int[] nums = {1, 2, 3, 4, 6, 12};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, bruteForceMaxSize(nums));
        assertEquals(4, result.size());
    }

    /**
     * 测试目的：验证大数值（10^9 量级）下的整除判断没有精度或溢出问题。
     * 测试用例：nums = [1000000000, 2000000000, 500000000]，构成 5*10^8 | 10^9 | 2*10^9 的链。
     * 预期结果：返回长度为 3 的合法整除子集。
     */
    @Test
    @DisplayName("正常输入-10^9 量级大数 [1000000000,2000000000,500000000]")
    public void testLargeValues() {
        int[] nums = {1000000000, 2000000000, 500000000};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 3);
    }

    // ------------------------------------------------------------------
    // 二、边界值：最小值、最大值、临界长度
    // ------------------------------------------------------------------

    /**
     * 测试目的：边界值分析，验证数组长度为约束下界 1 且元素取最小值 1 的情况。
     * 测试用例：nums = [1]。
     * 预期结果：返回 [1]。
     */
    @Test
    @DisplayName("边界值-单元素最小取值 [1]")
    public void testSingleElementMinValue() {
        List<Integer> result = solution.largestDivisibleSubset(new int[]{1});
        assertEquals(Arrays.asList(1), result);
    }

    /**
     * 测试目的：边界值分析，验证元素取约束上界 2*10^9 时的处理。
     * 测试用例：nums = [2000000000]。
     * 预期结果：返回 [2000000000]。
     */
    @Test
    @DisplayName("边界值-单元素取值上界 [2000000000]")
    public void testSingleElementUpperBoundValue() {
        List<Integer> result = solution.largestDivisibleSubset(new int[]{2000000000});
        assertEquals(Arrays.asList(2000000000), result);
    }

    /**
     * 测试目的：健壮性边界，元素取 int 类型的最大值（超出题目约束），验证不会发生数值溢出错误。
     * 测试用例：nums = [Integer.MAX_VALUE]。
     * 预期结果：返回 [2147483647]，且不抛异常。
     */
    @Test
    @DisplayName("边界值-单元素 int 最大值")
    public void testSingleElementIntMax() {
        List<Integer> result = solution.largestDivisibleSubset(new int[]{Integer.MAX_VALUE});
        assertEquals(Arrays.asList(Integer.MAX_VALUE), result);
    }

    /**
     * 测试目的：边界值分析，验证长度为 2 且两元素可整除时能识别出完整链。
     * 测试用例：nums = [9,3]。
     * 预期结果：返回 [3,9]（顺序不限），长度为 2。
     */
    @Test
    @DisplayName("边界值-两元素可整除 [9,3]")
    public void testTwoElementsDivisible() {
        int[] nums = {9, 3};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 2);
    }

    /**
     * 测试目的：边界值分析，验证长度为 2 且两元素不可整除时退化为单元素子集。
     * 测试用例：nums = [4,6]（6 % 4 != 0，4 % 6 != 0）。
     * 预期结果：返回长度为 1 的列表。
     */
    @Test
    @DisplayName("边界值-两元素不可整除 [4,6]")
    public void testTwoElementsNotDivisible() {
        int[] nums = {4, 6};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 1);
    }

    /**
     * 测试目的：边界值分析，验证数组长度取约束上界 1000 时算法仍能正确、及时完成（O(n^2)）。
     * 测试用例：nums = [1,2,3,...,1000]。由于每接一个元素至少翻倍，最长链为 1,2,4,...,512，长度 10。
     * 预期结果：返回长度为 10 的合法整除子集。
     */
    @Test
    @DisplayName("边界值-最大规模 n=1000")
    public void testMaxLengthInput() {
        int n = 1000;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = i + 1;
        }
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidDivisibleSubset(nums, result);
        assertEquals(10, result.size(), "1~1000 的最长整除链长度应为 10，实际返回 " + result);
    }

    /**
     * 测试目的：边界值分析，验证「最小值 1」与「最大值 2*10^9」同时出现的组合边界。
     * 测试用例：nums = [1, 2000000000]。
     * 预期结果：返回 [1, 2000000000]（顺序不限），长度为 2。
     */
    @Test
    @DisplayName("边界值-值域两端组合 [1,2000000000]")
    public void testValueRangeBothEnds() {
        int[] nums = {1, 2000000000};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 2);
    }

    // ------------------------------------------------------------------
    // 三、非法输入与异常处理
    // ------------------------------------------------------------------

    /**
     * 测试目的：异常处理测试，验证 null 输入被转换为语义明确的 IllegalArgumentException。
     * 测试用例：nums = null。
     * 预期结果：抛出 IllegalArgumentException（而不是 NullPointerException）。
     */
    @Test
    @DisplayName("非法输入-null")
    public void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> solution.largestDivisibleSubset(null));
    }

    /**
     * 测试目的：回归测试（Bug 5），验证空数组输入不会造成负长度数组或下标越界。
     * 测试用例：nums = new int[0]。
     * 预期结果：返回空列表，不抛任何异常（修复前抛出 NegativeArraySizeException）。
     */
    @Test
    @DisplayName("非法输入-空数组（回归 Bug 5）")
    public void testEmptyArray() {
        List<Integer> result = assertDoesNotThrow(() -> solution.largestDivisibleSubset(new int[0]));
        assertNotNull(result);
        assertTrue(result.isEmpty(), "空数组应返回空列表");
    }

    // ------------------------------------------------------------------
    // 四、特殊输入
    // ------------------------------------------------------------------

    /**
     * 测试目的：特殊输入测试，验证「除 1 之外各元素互不整除」时，链长度为 2（1 + 任一质数）。
     * 测试用例：nums = [1,13,17,19]。
     * 预期结果：返回长度为 2 的合法整除子集，且必须包含 1。
     */
    @Test
    @DisplayName("特殊输入-1 与多个质数 [1,13,17,19]")
    public void testOneWithPrimes() {
        int[] nums = {1, 13, 17, 19};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, bruteForceMaxSize(nums));
        assertEquals(2, result.size());
        assertTrue(result.contains(1), "长度为 2 的最优解必须包含 1");
    }

    /**
     * 测试目的：特殊输入测试，验证完全降序的 2 的幂序列（整除链最长且唯一）。
     * 测试用例：nums = [512,256,128,64,32,16,8,4,2,1]。
     * 预期结果：返回全部 10 个元素（顺序不限）。
     */
    @Test
    @DisplayName("特殊输入-2 的幂降序序列")
    public void testPowersOfTwoDescending() {
        int[] nums = {512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 10);
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512)),
                new HashSet<Integer>(result));
    }

    // ------------------------------------------------------------------
    // 五、回归测试：每个已修复的 Bug 至少一个用例
    // ------------------------------------------------------------------

    /**
     * 测试目的：回归测试（Bug 1：len = nums.length - 1 造成少处理最后一个元素）。
     * 测试用例：nums = [1,2,4,8,16]。修复前 len = 4，排序后的最大元素 16 永远不参与计算，
     *           最优解被算成 4 个元素而非 5 个。
     * 预期结果：返回长度为 5 的合法整除子集。
     */
    @Test
    @DisplayName("回归测试-Bug1 数组长度少算 1")
    public void testRegressionBug1OffByOneLength() {
        int[] nums = {1, 2, 4, 8, 16};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 5);
        assertEquals(5, result.size(), "最大元素 16 必须参与计算");
    }

    /**
     * 测试目的：回归测试（Bug 1 + Bug 2：len 为 0 且 maxVal = dp[0] 造成下标越界）。
     * 测试用例：nums = [5]（长度为 1，修复前 len = 0，dp 为空数组，取 dp[0] 抛异常）。
     * 预期结果：返回 [5]，不抛任何异常。
     */
    @Test
    @DisplayName("回归测试-Bug1/Bug2 单元素输入不得越界")
    public void testRegressionBug1SingleElementNoException() {
        List<Integer> result = assertDoesNotThrow(() -> solution.largestDivisibleSubset(new int[]{5}));
        assertEquals(Arrays.asList(5), result);
    }

    /**
     * 测试目的：回归测试（Bug 3：内层循环从 j = 1 开始，漏掉了排序后的首元素 nums[0]）。
     * 测试用例：nums = [1,2,4]。修复前 j 从 1 开始，1 永远无法作为前驱，
     *           dp 变成 [1,1,2]，最优解长度被算成 2 而不是 3。
     * 预期结果：返回 [1,2,4]（顺序不限），长度为 3。
     */
    @Test
    @DisplayName("回归测试-Bug3 内层循环漏掉 j=0")
    public void testRegressionBug3InnerLoopStartsAtZero() {
        int[] nums = {1, 2, 4};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, 3);
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 4)), new HashSet<Integer>(result));
    }

    /**
     * 测试目的：回归测试（Bug 4：maxVal = i 记录的是下标而不是元素值，导致倒推时整除判断失真）。
     * 测试用例：nums = [1,2,4,8,16,3,9]。修复前 maxVal 被写成一个 0~5 的下标，
     *           只能用「下标」去和元素值做取模，倒推出的元素个数少于最优长度。
     * 预期结果：返回长度等于暴力枚举基准（5，即 1-2-4-8-16）的合法整除子集。
     */
    @Test
    @DisplayName("回归测试-Bug4 maxVal 误用下标")
    public void testRegressionBug4MaxValUsesValue() {
        int[] nums = {1, 2, 4, 8, 16, 3, 9};
        List<Integer> result = solution.largestDivisibleSubset(nums.clone());
        assertValidAndOptimal(nums, result, bruteForceMaxSize(nums));
        assertEquals(5, result.size());
    }

    /**
     * 测试目的：回归测试（Bug 6：null 输入未处理，修复前直接抛 NullPointerException）。
     * 测试用例：nums = null。
     * 预期结果：抛出 IllegalArgumentException，消息中说明参数不能为空。
     */
    @Test
    @DisplayName("回归测试-Bug6 null 输入抛 IllegalArgumentException")
    public void testRegressionBug6NullInputMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> solution.largestDivisibleSubset(null));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("null"), "异常信息应说明参数为 null");
    }

    /**
     * 测试目的：综合回归，验证「修复后仍然不修改调用方语义上的正确答案」——
     *           对一组随机化但固定种子的输入，逐一用暴力枚举基准校验最优性。
     * 测试用例：由固定种子生成的 12 组规模 1~10 的随机集合（可复现）。
     * 预期结果：每组都返回合法且长度等于暴力枚举基准的整除子集。
     */
    @Test
    @DisplayName("回归测试-随机化输入与暴力枚举基准逐一比对")
    public void testRandomizedAgainstBruteForce() {
        java.util.Random random = new java.util.Random(2024112758L);
        for (int round = 0; round < 12; round++) {
            int size = 1 + random.nextInt(10);
            Set<Integer> set = new HashSet<Integer>();
            while (set.size() < size) {
                set.add(1 + random.nextInt(60));
            }
            int[] nums = new int[size];
            int idx = 0;
            for (Integer v : set) {
                nums[idx++] = v;
            }
            int[] copy = nums.clone();
            List<Integer> result = solution.largestDivisibleSubset(nums);
            assertValidAndOptimal(copy, result, bruteForceMaxSize(copy));
        }
    }
}
