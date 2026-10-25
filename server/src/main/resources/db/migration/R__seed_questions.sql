-- Repeatable migration: re-runs automatically whenever this file changes.
-- Every statement is an upsert keyed by slug, so edits are applied in place.
-- NOTE: removing an option from a question here does NOT delete the old row;
-- add a normal migration for that. Questions are never deleted by this file.
-- All questions below are SAMPLE content: review and replace before real use.

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$hashmap-lookup-avg$q$, $q$Hash tables$q$, 1, $q$MCQ$q$, $q$What is the average-case time complexity of looking up a key in a hash map?$q$, NULL, $q$A good hash function spreads keys across buckets, so a lookup jumps straight to (nearly) the right bucket: O(1) on average. Worst case with many collisions degrades toward O(n).$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$O(1)$q$, TRUE),
        ($q$b$q$, 2, $q$O(log n)$q$, FALSE),
        ($q$c$q$, 3, $q$O(n)$q$, FALSE),
        ($q$d$q$, 4, $q$O(n log n)$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$lifo-structure$q$, $q$Stacks & queues$q$, 1, $q$MCQ$q$, $q$Which data structure follows last-in, first-out (LIFO) order?$q$, NULL, $q$A stack removes the most recently added item first (think of undo history or the call stack). A queue is first-in, first-out.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$Queue$q$, FALSE),
        ($q$b$q$, 2, $q$Stack$q$, TRUE),
        ($q$c$q$, 3, $q$Min-heap$q$, FALSE),
        ($q$d$q$, 4, $q$Hash map$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$py-slice-len$q$, $q$Python basics$q$, 1, $q$CODE_OUTPUT$q$, $q$What does this print?$q$, $q$print(len([1, 2, 3][1:]))$q$, $q$`[1, 2, 3][1:]` is a slice from index 1 to the end: `[2, 3]`, which has length 2.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$1$q$, FALSE),
        ($q$b$q$, 2, $q$2$q$, TRUE),
        ($q$c$q$, 3, $q$3$q$, FALSE),
        ($q$d$q$, 4, $q$It raises an error$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$binary-search-precondition$q$, $q$Binary search$q$, 1, $q$MCQ$q$, $q$What must be true about the input for binary search to work?$q$, NULL, $q$Binary search discards half the range by comparing to the middle element, which only makes sense if the data is sorted. Duplicates, negatives and odd lengths are all fine.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$It is sorted$q$, TRUE),
        ($q$b$q$, 2, $q$All elements are unique$q$, FALSE),
        ($q$c$q$, 3, $q$All elements are positive$q$, FALSE),
        ($q$d$q$, 4, $q$Its length is a power of two$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$py-string-repeat$q$, $q$Python basics$q$, 1, $q$CODE_OUTPUT$q$, $q$What does this print?$q$, $q$print(3 * "ab")$q$, $q$Multiplying a string by an integer repeats it: `3 * "ab"` is `"ababab"`.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$ab3$q$, FALSE),
        ($q$b$q$, 2, $q$ababab$q$, TRUE),
        ($q$c$q$, 3, $q$aaabbb$q$, FALSE),
        ($q$d$q$, 4, $q$It raises an error$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$binary-search-complexity$q$, $q$Binary search$q$, 2, $q$MCQ$q$, $q$What is the worst-case time complexity of binary search on a sorted array of n elements?$q$, NULL, $q$Each step halves the remaining range, so at most about log2(n) steps are needed.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$O(1)$q$, FALSE),
        ($q$b$q$, 2, $q$O(log n)$q$, TRUE),
        ($q$c$q$, 3, $q$O(n)$q$, FALSE),
        ($q$d$q$, 4, $q$O(n log n)$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$two-sum-hashmap$q$, $q$Hash tables$q$, 2, $q$MCQ$q$, $q$You solve Two Sum by walking the array once and storing each number in a hash map to look up its complement. What are the time and space complexity?$q$, NULL, $q$One pass with O(1) average lookups is O(n) time. The map can hold up to n entries, so O(n) space. The O(n^2) / O(1) version is the brute-force pair check.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$O(n) time, O(1) space$q$, FALSE),
        ($q$b$q$, 2, $q$O(n) time, O(n) space$q$, TRUE),
        ($q$c$q$, 3, $q$O(n^2) time, O(1) space$q$, FALSE),
        ($q$d$q$, 4, $q$O(n log n) time, O(n) space$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$py-mutable-default$q$, $q$Python gotchas$q$, 2, $q$CODE_OUTPUT$q$, $q$What does this print?$q$, $q$def f(x, acc=[]):
    acc.append(x)
    return acc

print(f(1), f(2))$q$, $q$The default list is created once and shared by every call. Both calls return that same list, and `print` runs after both have appended, so it shows `[1, 2]` twice.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$[1] [2]$q$, FALSE),
        ($q$b$q$, 2, $q$[1, 2] [1, 2]$q$, TRUE),
        ($q$c$q$, 3, $q$[1] [1, 2]$q$, FALSE),
        ($q$d$q$, 4, $q$It raises an error$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$bst-inorder$q$, $q$Trees$q$, 2, $q$MCQ$q$, $q$Which traversal of a binary search tree visits the values in sorted order?$q$, NULL, $q$In-order visits left subtree, node, then right subtree. In a BST that yields values from smallest to largest.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$Pre-order$q$, FALSE),
        ($q$b$q$, 2, $q$In-order$q$, TRUE),
        ($q$c$q$, 3, $q$Post-order$q$, FALSE),
        ($q$d$q$, 4, $q$Level-order$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$heapify-complexity$q$, $q$Heaps$q$, 3, $q$MCQ$q$, $q$What is the time complexity of building a binary heap from an unsorted array of n elements using the bottom-up (heapify) method?$q$, NULL, $q$Most nodes sit near the bottom and need very little sifting, so the total work sums to O(n), not O(n log n). Inserting the elements one by one would be O(n log n).$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$O(n log n)$q$, FALSE),
        ($q$b$q$, 2, $q$O(n)$q$, TRUE),
        ($q$c$q$, 3, $q$O(log n)$q$, FALSE),
        ($q$d$q$, 4, $q$O(n^2)$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$py-remove-while-iterating$q$, $q$Python gotchas$q$, 3, $q$CODE_OUTPUT$q$, $q$What does this print?$q$, $q$nums = [1, 2, 3]
for n in nums:
    if n == 2:
        nums.remove(n)
print(nums)$q$, $q$Removing 2 shifts 3 into index 1, but the loop's hidden index moves on to 2 and finds the list has ended, so 3 is never visited. The result is `[1, 3]`. Modifying a list while iterating it is a classic bug.$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$[1, 2, 3]$q$, FALSE),
        ($q$b$q$, 2, $q$[1, 3]$q$, TRUE),
        ($q$c$q$, 3, $q$[1]$q$, FALSE),
        ($q$d$q$, 4, $q$It raises an error$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;

WITH q AS (
    INSERT INTO questions (slug, topic, difficulty, kind, prompt_md, code_snippet, explanation_md, status)
    VALUES ($q$dynamic-array-append$q$, $q$Arrays$q$, 3, $q$MCQ$q$, $q$A dynamic array (like Python's list) doubles its capacity when full. What is the amortized cost of append?$q$, NULL, $q$Resizing costs O(n) but happens rarely: after doubling, n more appends are cheap. Spread across all appends, the average cost per append is O(1).$q$, 'APPROVED')
    ON CONFLICT (slug) DO UPDATE SET
        topic = EXCLUDED.topic, difficulty = EXCLUDED.difficulty, kind = EXCLUDED.kind,
        prompt_md = EXCLUDED.prompt_md, code_snippet = EXCLUDED.code_snippet,
        explanation_md = EXCLUDED.explanation_md
    RETURNING id
)
INSERT INTO question_options (question_id, option_key, position, label, is_correct)
SELECT q.id, v.option_key, v.position, v.label, v.is_correct
FROM q, (VALUES
        ($q$a$q$, 1, $q$O(1)$q$, TRUE),
        ($q$b$q$, 2, $q$O(log n)$q$, FALSE),
        ($q$c$q$, 3, $q$O(n)$q$, FALSE),
        ($q$d$q$, 4, $q$O(n log n)$q$, FALSE)
     ) AS v (option_key, position, label, is_correct)
ON CONFLICT (question_id, option_key) DO UPDATE SET
    position = EXCLUDED.position, label = EXCLUDED.label, is_correct = EXCLUDED.is_correct;
