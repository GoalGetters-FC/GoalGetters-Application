const core = require('@actions/core');
const github = require('@actions/github');

(async () => {
    try {
        // Pass the GitHub context and core to your existing module
        await module.exports({ github, context: github.context, core });
    } catch (error) {
        core.setFailed(error.message);
    }
})();


module.exports = async ({ github, context, core }) => {
    const branch = retrieveBranchTitle(context);
    if (!branch) {
        core.setFailed(`Unable to determine branch name from context`);
        return false;
    }

    const result = validateBranchTitle(branch, core);
    if (result.valid) {
        const message = '## :heavy_check_mark: Conventional Branches'
        await comment(github, context, message, core);
    } else {
        const failures = result.failures.length ? `Failures:\n- ${result.failures.join(`\n- `)}` : ``;
        const warnings = result.warnings.length ? `Warnings:\n- ${result.warnings.join(`\n- `)}` : ``;
        const message = `## :x: Conventional Branches
        
**Branch:** \`${branch}\`
**Warnings:**
${warnings.map(i => `- ${i}`).join(`\n`) ?? `- None`}
**Failures:**
${failures.map(i => `- ${i}`).join(`\n`) ?? `- None`}
`

        await comment(github, context, message, core);
        core.setFailed(`Branch name "${branch}" does not meet the conventional branch naming rules`);
    }
};


// --- Internals


async function comment(github, context, body, core) {
    if (!RULESET.comment || !context.payload?.pull_request?.number) {
        return false;
    }

    for (let attempt = 1; attempt <= (RULESET.commentLimits.enabled ? RULESET.commentLimits.retries : 1); attempt++) { 
        try {
            await github.rest.issues.createComment({
                owner: context.repo.owner,
                issue_number: context.payload.pull_request.number,
                repo: context.repo.repo,
                body: body,
            });

            return true;
        } catch (error) {
            if (attempt < RULESET.commentLimits.retries) {
                const steppedDelay = (RULESET.commentLimits.delay * Math.pow(2, attempt - 1));
                await new Promise(resolve => setTimeout(resolve, steppedDelay));
            }
        }
    }

    return false;
}


function retrieveBranchTitle(context) {
    // Get source branch name from PR
    if (context.payload?.pull_request?.head?.ref)
        return context.payload.pull_request.head.ref;

    return (context.ref_name || null);
}


function validateBranchTitle(title, core) {
    const result = {
        valid: false,
        failures: [],
        warnings: [],
    }

    // CHECKS

    if (title.length < RULESET.minLength) {
        result.failures.push(`Length under minimum of ${RULESET.minLength} characters`);
    }

    if (title.length > RULESET.maxLength) {
        result.failures.push(`Length above maximum of ${RULESET.maxLength} characters`);
    }

    for (const pattern of RULESET.patterns) {
        if (pattern.regex.test(title)) {
            result.valid = true;
            break;
        }
    }

    if (!result.valid) {
        result.warnings.push(`Fails to meet any of the defined patterns`);
    }

    return result;
}


const RULESET = {
    minLength: 3,
    maxLength: 40,

    patterns: [
        {
            regex: /^main$/,
            description: `Long-lived production branch`
        },
        {
            regex: /^staging$/,
            description: `Long-lived production branch`
        },
        {
            regex: /^rollback\/[a-z0-9]+(?:-[a-z0-9]+)*$/,
            description: `For storing copies of back-tracked branches`
        },
        {
            regex: /^topic\/[a-z0-9]+(?:-[a-z0-9]+)*$/,
            description: `Short-lived feature branch`
        },
        {
            regex: /^tests\/[a-z0-9]+(?:-[a-z0-9]+)*$/,
            description: `Short-lived testing branch`
        },
    ],

    comment: true,
    commentLimits: {
        enabled: true,
        retries: 3,
        delay: 2000,
    }
};