import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 *
 * 给你一个由 无重复 正整数组成的集合 nums ，请你找出并返回其中最大的整除子集 answer ，子集中每一元素对 (answer[i], answer[j]) 都应当满足：
 * answer[i] % answer[j] == 0 ，或
 * answer[j] % answer[i] == 0
 * 如果存在多个有效解子集，返回其中任何一个均可。
 *
 *
 *
 * 示例 1：
 *
 * 输入：nums = [1,2,3]
 * 输出：[1,2]
 * 解释：[1,3] 也会被视为正确答案。
 * 示例 2：
 *
 * 输入：nums = [1,2,4,8]
 * 输出：[1,2,4,8]
 *
 *
 * 提示：
 *
 * 1 <= nums.length <= 1000
 * 1 <= nums[i] <= 2 * 10^9
 * nums 中的所有整数 互不相同
 *
 */
class Solution3 {

    /**
     * 求最大整除子集。
     *
     * <p>思路：整除关系具有传递性，先把数组升序排序，则任意整除链在排序后都是下标递增的序列
     * （后一个元素是前一个元素的倍数）。据此用动态规划求解：dp[i] 表示「以 nums[i] 结尾」的
     * 最长整除链长度，状态转移为
     * dp[i] = max{ dp[j] + 1 | 0 &lt;= j &lt; i 且 nums[i] % nums[j] == 0 }。
     * 求出最大长度及其对应的最大元素值后，自后向前倒推即可还原出一个最优子集。</p>
     *
     * <p>复杂度：时间 O(n^2)，空间 O(n)，n = nums.length &lt;= 1000。</p>
     *
     * @param nums 无重复正整数集合，长度 1~1000，元素取值范围 1~2*10^9，调用后该数组会被升序排序
     * @return 任意一个长度最大的整除子集；nums 为空数组时返回空列表
     * @throws IllegalArgumentException nums 为 null 时抛出
     */
    public List<Integer> largestDivisibleSubset(int[] nums) {
        // 边界处理：题目约束之外的非法输入给出语义明确的异常，而不是难以定位的空指针异常
        if (nums == null) {
            throw new IllegalArgumentException("nums 不能为 null");
        }

        List<Integer> res = new ArrayList<Integer>();
        // 边界处理：空集合中不存在非空整除子集；提前返回同时也避免了对空数组取下标
        if (nums.length == 0) {
            return res;
        }

        Arrays.sort(nums);
        int len = nums.length;

        // 第 1 步：动态规划找出最大子集的个数、最大子集中的最大整数
        int[] dp = new int[len];
        Arrays.fill(dp, 1);
        int maxSize = 1;
        // 注意：maxVal 记录的是「最优子集中的最大元素的值」，而不是下标
        int maxVal = nums[0];
        for (int i = 1; i < len; i++) {
            // 内层必须从 0 开始：nums 升序后 nums[0] 是最小元素，它能整除后面所有元素，
            // 跳过 j = 0 会漏掉所有「以最小元素开头」的整除链
            for (int j = 0; j < i; j++) {
                // 题目中说「没有重复元素」很重要
                if (nums[i] % nums[j] == 0) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }

            if (dp[i] > maxSize) {
                maxSize = dp[i];
                maxVal = nums[i];
            }
        }

        // 第 2 步：倒推获得最大子集
        // maxSize == 1 说明任意两个元素之间都不存在整除关系，此时返回任意一个元素都是最优解
        if (maxSize == 1) {
            res.add(nums[0]);
            return res;
        }

        // 自后向前扫描：当 dp[i] 恰好等于当前待补齐的链长，且 nums[i] 能整除当前链的最大元素时，
        // nums[i] 一定可以接入该链（dp[i] 本身保证存在一条长度为 dp[i] 且以 nums[i] 结尾的整除链）
        for (int i = len - 1; i >= 0 && maxSize > 0; i--) {
            if (dp[i] == maxSize && maxVal % nums[i] == 0) {
                res.add(nums[i]);
                maxVal = nums[i];
                maxSize--;
            }
        }
        return res;
    }
}
