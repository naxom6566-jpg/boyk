package com.example.data.repository

import com.example.data.model.PromptTemplateEntity

object DefaultPrompts {
    val list: List<PromptTemplateEntity> = listOf(
        // === WRITING ===
        PromptTemplateEntity(
            title = "Comprehensive Blog Post Writer",
            description = "Draft structured, engaging blog articles complete with research depth and natural SEO transitions.",
            prompt = "Act as an expert SEO copywriter. Write a comprehensive, SEO-optimized blog post about <topic>. Incorporate conversational transitions, authoritative resource citations, and bold key concepts. Structure the piece with an engaging introduction, structured body sub-headings, and a highly shareable concluding call-to-action.",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "AIDA Sales Copy Creator",
            description = "Write sales emails or landing page text leveraging Attention, Interest, Desire, Action framework.",
            prompt = "Write high-converting sales copy for <product/service> using the classic AIDA structure. Ensure you lead with an attention-grabbing hook, foster genuine interest through addressing pain points, build powerful desire with unique selling benefits, and close with an urgent action trigger.",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "Strategic Email Sequence Generator",
            description = "Generate multi-step welcome or cold marketing email funnels based on persona traits.",
            prompt = "Design a 3-part email welcome sequence for users signing up for <service>. The tone should be welcoming, professional, and outcome-oriented. Part 1 focuses on value delivery, Part 2 on addressing industry pain points, and Part 3 on driving conversion.",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "Immersive Story World-Builder",
            description = "Flesh out story premises with deep character arcs, environment sensory descriptions, and plots.",
            prompt = "Flesh out the narrative premise: <premise>. Create distinct character profiles, define sensory landscape designs, describe a pivotal internal conflict, and weave in subtle foreshadowing hints.",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "LinkedIn Thought Leadership Post",
            description = "Formulate high-performance, spaced LinkedIn updates showcasing trade expertise.",
            prompt = "Draft an engaging LinkedIn post regarding recent lessons learned in <industry/topic>. Use an direct hook, short spaced paragraphs, crisp bullet points, and prompt users with an open-ended question to maximize comment thread engagement.",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "In-Depth Academic Essay Outline",
            description = "Generate comprehensive structural outlines and thesis definitions for argumentative papers.",
            prompt = "Construct an academic argumentative essay outline on <thesis_question>. Provide a robust thesis statement definition, section-by-section transition strategies, and key evidence requirements for each argument.",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "PR Media Press Release",
            description = "Draft formal newsworthy announcements following standard inverse-pyramid structures.",
            prompt = "Compose an official press release announcing <company_update>. Use the standard journalistic style: strong headline, dateline, lead summary, supporting executive quotes, boilerplate background, and clear media contact details.",
            category = "Writing"
        ),

        // === CODING ===
        PromptTemplateEntity(
            title = "Senior Code Review & Optimization",
            description = "Audit lines for memory performance, security exploits, style conventions, and bottlenecks.",
            prompt = "Act as a Senior Staff Engineer. Conduct an exhaustive code review of the following block: \n\n<code>\n\nAnalyze efficiency bottleneck potentials, potential memory leaks, logic errors, and code styling patterns. Provide highly optimized, refactored alternatives along with architectural logic explanations.",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "Stupid-Simple Bug Hunter",
            description = "Locate edge-cases, off-by-one errors, state races, and compile issues in complex code blocks.",
            prompt = "Identify potential edge-cases and critical bugs in this code block: \n\n<code>\n\nList line-by-line failure paths, unexpected input reactions, and write robust unit tests to safely isolate the issue.",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "Clean Architectural Refactoring",
            description = "Transform messy procedures into modular SOLID components with testable abstractions.",
            prompt = "Refactor this legacy code to strictly follow clean architecture guidelines, SOLID principles, and clean separation of concerns. Break large functions into modular units and add clean interfaces:\n\n<code>",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "RESTful API Integration Designer",
            description = "Formulate comprehensive endpoint signatures, requests, JSON payloads, and error handlers.",
            prompt = "Design a robust REST API endpoint suite for <process/service>. Produce clean OpenAPI specifications, detailing JSON request/response formats, status codes, query criteria, and comprehensive error handling wrappers.",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "SQL Expert Query Generator",
            description = "Draft high-performance relational database joints, index plans, and partitions.",
            prompt = "Write an optimized, highly performant SQL query to achieve this result: <analytics_goal>. Include index recommendations, visual explain-plan explanations, and guard against heavy join performance degradation.",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "Comprehensive Security Code Auditor",
            description = "Scan systems for OWASP-10 bugs, prompt injection vulnerabilities, CSRF, and leaks.",
            prompt = "Audit this code block for security risks, covering SQL injections, cross-site scripting, improper validation, and credentials leakage: \n\n<code>",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "Automated Unit Test Suite Generator",
            description = "Create complete test fixtures with robust mocks, happy states, and edge inputs.",
            prompt = "Generate a comprehensive suite of unit test cases for the following function, covering happy-path actions, empty inputs, null targets, and high boundaries: \n\n<code>",
            category = "Coding"
        ),

        // === BUSINESS ===
        PromptTemplateEntity(
            title = "One-Page Agile Business Model Canvas",
            description = "Outline product value propositions, stream configurations, channels, and resource costs.",
            prompt = "Formulate a comprehensive Business Model Canvas for <idea>. Cover customer segments, values offered, key cost structures, distribution channels, and diversified streams of revenue.",
            category = "Business"
        ),
        PromptTemplateEntity(
            title = "Go-To-Market Growth Strategy Outline",
            description = "Devise high-impact onboarding paths, organic virality loops, and paid acquisition tactics.",
            prompt = "Generate a comprehensive Go-To-Market strategy outline for introducing <product> to <target_audience>. Outline customer acquisition channels, organic virality components, and customer loyalty retention strategies.",
            category = "Business"
        ),
        PromptTemplateEntity(
            title = "SWOT & Competitor Radar Matrix",
            description = "Map internal capabilities and external market shifts to devise defensive pivots.",
            prompt = "Create a detailed SWOT Matrix (Strengths, Weaknesses, Opportunities, Threats) for <business/niche>. Propose action items to convert weaknesses into unique opportunities and hedge against threats.",
            category = "Business"
        ),
        PromptTemplateEntity(
            title = "Venture Pitch Deck Narrative Builder",
            description = "Craft a compelling 10-slide narrative progression from problem hook to financial projections.",
            prompt = "Draft a 10-slide venture capitalist pitch deck narrative outline for <venture>. Specify the focal message, key visual recommendations, and script bullet points for each slide.",
            category = "Business"
        ),
        PromptTemplateEntity(
            title = "Cold Enterprise Sales Developer Email",
            description = "Draft concise, personalization-friendly outreach emails targeting executive Decision-Makers.",
            prompt = "Write an ultra-personalized cold sales outreach email tailored for a busy C-level executive at <company_type> offering <solution>. Write a brief hook, state concrete ROI data, and offer a low-friction call-to-action.",
            category = "Business"
        ),
        PromptTemplateEntity(
            title = "Operational Cost-Reduction Strategy",
            description = "Formulate pathways to automate overhead, streamline workflows, and locate waste.",
            prompt = "Formulate a systematic efficiency framework and cost-containment strategy for my business in <industry>. Highlight the highest opportunities for automated tooling and lean process modifications.",
            category = "Business"
        ),

        // === PRODUCTIVITY ===
        PromptTemplateEntity(
            title = "Eisenhower Matrix Prioritizer",
            description = "Sort cluttered todo pipelines into actionable urgent, strategic, delegate, or discard quadrants.",
            prompt = "Act as an executive productivity strategist. Organize the following raw list of tasks into the four specific Eisenhower quadrants (Urgent/Important, Important/Not Urgent, Urgent/Not Important, Neither). Suggest a sequence for action focus:\n\n<tasks>",
            category = "Productivity"
        ),
        PromptTemplateEntity(
            title = "Raw Transcript Summarizer",
            description = "Distill unformatted text logs into action items, meeting topics, and clear summary briefs.",
            prompt = "Synthesize the following chaotic meeting transcript. Produce a clean, structured high-level executive summary, categorizing central decisions made, assigning clear action owners, and outlining the next steps:\n\n<transcript>",
            category = "Productivity"
        ),
        PromptTemplateEntity(
            title = "Feynman Technique Study Guide",
            description = "Break complex technologies down into core intuitive analogies a kid can safely understand.",
            prompt = "Deconstruct the complex concept <topic> using the Feynman Technique. Explain it step-by-step using highly practical everyday analogies. Avoid any specialized jargon.",
            category = "Productivity"
        ),
        PromptTemplateEntity(
            title = "Hyper-Focused Daily Timeblock Planner",
            description = "Map goals into a distraction-free schedule balancing shallow administrative work and deep-focus sessions.",
            prompt = "Create a rigorous day-long timeblock schedule for completing these goals: <goals>. Build in buffer times, block intervals for deep creative focus, and schedule a solid end-of-day shutdown routine.",
            category = "Productivity"
        ),
        PromptTemplateEntity(
            title = "Weekly Personal Review Retrospective",
            description = "Synthesize lessons, log habits, track projects, and realign objectives.",
            prompt = "Guide me through a structural weekly personal retro. Ask leading questions about challenges overcome, progress towards high-level goals, and create a system to realign my focus fields for the coming week.",
            category = "Productivity"
        ),
        PromptTemplateEntity(
            title = "Goal Breakdown & Milestone Architect",
            description = "Deconstruct big projects into bite-sized actionable milestones with sprint structures.",
            prompt = "Break down this massive project idea: <project>. Design step-by-step 2-week sprint milestones, detailing explicit tasks and definition-of-done criteria.",
            category = "Productivity"
        ),

        // === EDUCATION ===
        PromptTemplateEntity(
            title = "Personalized Language Tutor",
            description = "Construct progressive dialogues and contextual vocabulary lists with usage notes.",
            prompt = "Act as an expert native language teacher in <language>. Design a practical conversational dialogue lesson centered around <real_life_scenario>. Include common idioms, contextual vocabulary explanations, and grammatical challenges.",
            category = "Education"
        ),
        PromptTemplateEntity(
            title = "Active-Recall Flashcard Generator",
            description = "Convert study topics into question/answer flashcards optimized for spaced repetition.",
            prompt = "Create a series of 15 high-fidelity, active-recall Q&A study cards for <topic>. Design them to isolate individual key definitions, preventing cognitive overload and ensuring quick recall cycles.",
            category = "Education"
        ),
        PromptTemplateEntity(
            title = "Curriculum Builder & Course Architect",
            description = "Structure a comprehensive, semester-long learning syllabus from scratch for any academic domain.",
            prompt = "Design a detailed 10-week comprehensive curriculum syllabus to self-educate in <field>. Format each week with specific core reading themes, practical exercise setups, and research objectives.",
            category = "Education"
        ),
        PromptTemplateEntity(
            title = "Socratic Dialogue Partner",
            description = "Engage in inquiry-driven debates to test conceptual depth on philosophical/social issues.",
            prompt = "Act as an expert Socratic interlocutor. Ask me thought-provoking, critical, open-ended questions one at a time regarding <theory_or_belief> to help me test and refine the depth of my logic.",
            category = "Education"
        ),
        PromptTemplateEntity(
            title = "Academic Lesson Planner",
            description = "Formulate engaging learning plans with objectives, group tasks, and assessments.",
            prompt = "Draft a comprehensive lesson plan for teaching <topic> to <grade_level/audience>. State key learning objectives, set interactive classroom group exercises, and outline formative check assessment steps.",
            category = "Education"
        ),

        // === DESIGN ===
        PromptTemplateEntity(
            title = "Interactive Mobile UI Clinic",
            description = "Critique screen layouts against modern Material 3/UX usability principles.",
            prompt = "Provide a comprehensive critique of an interface showing <screen_description>. Review visual hierarchy, accessibility, button size ergonomics, cognitive friction, and suggest concrete fixes.",
            category = "Design"
        ),
        PromptTemplateEntity(
            title = "Accessible Color Palette Architect",
            description = "Generate WCAG contrast-compliant color codes for light and dark themes.",
            prompt = "Create an elegant primary color palette for a brand in <industry> focusing on <brand_vibe>. Provide primary, secondary, and background hex codes for both Light and Dark layouts, verifying WCAG 2.1 AAA accessibility.",
            category = "Design"
        ),
        PromptTemplateEntity(
            title = "Brand Sensory Identity & Typography",
            description = "Formulate type pairings and tone descriptors fitting customer expectations.",
            prompt = "Formulate a cohesive brand design handbook for <business>. Recommend specific Google Fonts pairings, describe the voice guidelines, and suggest visual treatment layouts that convey <desired_feeling>.",
            category = "Design"
        ),
        PromptTemplateEntity(
            title = "UX Microcopy & Tone of Voice Optimizer",
            description = "Polish UI snackbars, success modals, and transactional notifications for high clarity.",
            prompt = "Improve the microcopy for these in-app touchpoints (Modals, empty-state headers, snackbars) to deliver a clear, friendly experience for <user_action/flow>:\n\n<microcopy>",
            category = "Design"
        ),
        PromptTemplateEntity(
            title = "Logo Ideation Concept Guide",
            description = "Synthesize cultural motifs and geometries to outline unique logo concept proposals.",
            prompt = "Pitch 5 distinct, highly memorable logo concepts for <venture>. Specify the geometric symbol compositions, metaphor meanings, and visual placement suggestions.",
            category = "Design"
        ),

        // === SOCIAL MEDIA ===
        PromptTemplateEntity(
            title = "Viral Short-Form Script Outline",
            description = "Formulate high-hook scripts for TikTok, Reels, or YouTube Shorts under 60s.",
            prompt = "Draft a 60-second highly engaging video script about <topic>. Format with a retention-maximizing hook in the first 3 seconds, dynamic visual layout cues, and a fast, clear closing action prompt.",
            category = "Social Media"
        ),
        PromptTemplateEntity(
            title = "Strategic Viral X/Twitter Thread",
            description = "Craft high-leverage thread structures formatting with hooks and pacing.",
            prompt = "Construct a high-performance 7-tweet X thread explaining <educational_topic>. Ensure Tweet 1 acts as a viral hook, Tweets 2-6 deliver standalone value points, and Tweet 7 promotes my project.",
            category = "Social Media"
        ),
        PromptTemplateEntity(
            title = "Strategic Monthly Content Calendar",
            description = "Synthesize audience pain points into 30 diverse multi-format post ideas.",
            prompt = "Create a 4-week thematic social media content calendar for <niche>. Map topics to varied formats (Carousel, Video script, text hook) and outline key engagement schedules.",
            category = "Social Media"
        ),
        PromptTemplateEntity(
            title = "YouTube Video Script Blueprint",
            description = "Draft full-length scripts mapping attention retention spikes, intros, and callouts.",
            prompt = "Synthesize an in-depth 8-minute YouTube video outline for <topic>. Format with an attention hook, visual presentation prompts, educational narrative segments, and a mid-roll conversion CTA.",
            category = "Social Media"
        ),
        PromptTemplateEntity(
            title = "High-Trust Instagram Story Series",
            description = "Outline day-long engagement sequences leveraging interactive sticker prompts.",
            prompt = "Plan a 5-step Instagram story sequence for launching <announcement>. Detail visual frame layouts, copy hooks, and suggest specific interactive poll stickers to maximize participation.",
            category = "Social Media"
        ),

        // === AI CREATION ===
        PromptTemplateEntity(
            title = "Hyper-Realistic Midjourney Prompter",
            description = "Write visually stunning image generation prompts complete with ratios, cameras, lights, styles.",
            prompt = "Produce 3 highly detailed, descriptive Midjourney prompts for <scene_idea>. Incorporate photorealistic keywords, cinematic lighting (depth of field, atmospheric haze), lens criteria (85mm, f/1.8), specific aspect ratios (--ar 16:9), and render parameters.",
            category = "AI Creation"
        ),
        PromptTemplateEntity(
            title = "Stable Diffusion Styling Blueprint",
            description = "Formulate high-density positive and negative prompts including style weight descriptors.",
            prompt = "Formulate an advanced Stable Diffusion XL prompt for generating a high-quality visual of <concept>. Compile both high-priority positive descriptors and an optimized negative prompt to block artifacts, blur, and distortions.",
            category = "AI Creation"
        ),
        PromptTemplateEntity(
            title = "Cinematic Luma/Runway Video Prompt",
            description = "Draft motion vectors, lighting states, and transitions for next-gen video models.",
            prompt = "Draft a highly descriptive text-to-video scene script tailored for Runway Gen-3 or Luma Dream Machine. Use dynamic kinetic cues, precise camera pans (slow dolly-in, orbit sweep), lighting gradients, and particle mechanics.",
            category = "AI Creation"
        ),
        PromptTemplateEntity(
            title = "AI Character Persona Profile Creator",
            description = "Construct comprehensive system instructions containing traits, linguistic preferences, and guardrails.",
            prompt = "Construct an exhaustive AI persona specification block for acting as <character_identity>. Define their core conversational traits, speech patterns, vocabulary constraints, and robust behavioral boundaries.",
            category = "AI Creation"
        ),
        PromptTemplateEntity(
            title = "Premium Vector Flat Illustration Prompt",
            description = "Draft prompts targeting clean vector shapes, specific hex ranges, and modern iconography styles.",
            prompt = "Generate 3 highly optimized SVG/vector art generator prompts to create icons representing <concepts>. Specify flat colors, clean geometric boundaries, soft isometric views, and transparency.",
            category = "AI Creation"
        ),
        PromptTemplateEntity(
            title = "Regex Code Pattern Finder",
            description = "Draft patterns for matching precise targets, parsing schemas, and token validation.",
            prompt = "Act as an expert regex compiler. Draft a highly precise regular expression to extract <target_tokens> from the following logs: <example_inputs>. Explain every group, lookahead, and boundary checker included.",
            category = "Coding"
        ),
        PromptTemplateEntity(
            title = "Novel Story Arc Blueprint",
            description = "Plot full multi-chapter story beats with standard Hero's Journey or Fichtean outlines.",
            prompt = "Map an immersive 12-chapter novel skeleton detailing major plot milestones, inciting incidents, and dynamic core character shifts: <premise>",
            category = "Writing"
        ),
        PromptTemplateEntity(
            title = "Elevator Pitch Synthesizer",
            description = "Summarize complex technology into a high-impact, memorable 30-second speaking script.",
            prompt = "Synthesize this business ideas into a high-retention 30-second elevator pitch using clear, outcome-first wording targeting <executive_persona>: <business_idea>",
            category = "Business"
        ),
        PromptTemplateEntity(
            title = "Daily Standup Reporter",
            description = "Format unstructured task logs into professional, clear Jira or Slack update boards.",
            prompt = "Act as an agile product team member. Format this messy, direct diary entries into a polished, crisp daily standup report: <diary_log>",
            category = "Productivity"
        ),
        PromptTemplateEntity(
            title = "History Analogy Tutor",
            description = "Break down complex past historical events with powerful interactive and modern analogies.",
            prompt = "Act as a modern history instructor. Explain the complex historical events of <event> using highly relatable modern analogies to make lessons memorable.",
            category = "Education"
        )
    )
}
