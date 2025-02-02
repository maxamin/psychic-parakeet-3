## Security Review Overview

#### What is a security review
- There are a multitude of strategies for approaching a sec review.
- Stage 1: Contact - contact between parties is made, ideally occuring prior to code being 100% ready for review to allow time for preliminary considerations.
- Stage 2: Initial assessment - Security Researcher peforms an initial assessment of the code base, evaluating scope and code complexity to establish an estimated duration/timeline for the review.
- Stage 3: Confirmation - Get codebase commit hash, finalize price, get downpayment, and provide start date.
- Stage 4: Security review - find vulns.
- Stage 5: Initial report - After the allotted time period ends, SR provides client with initial report detailing findings listed by severity; H/M/L/informational-non-critical/Gas.
- Stage 6: Mitigation - client team works to address findings of the initial report within a predefined time frame, usually much short than the review itself.
- Stage 7: Final report - SR performs final report exclusively on the fixes made to address the issues raised in the initial report.
   
#### Key considerations during initial asssesment
- Does the protocol have clear documentation?
- Is there a robust test suite including fuzz tests?
- Is the code commented and readable?
- Are modern best practices followed?
- Will there be a reliable communication channel between SR and devs?
- Can an initial video walkthrough be provided?  
