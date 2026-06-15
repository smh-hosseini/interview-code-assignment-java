# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

Accurately tracking and allocating costs in a fulfillment setup is tricky because many expenses — like labor, transport, and overhead — are shared across warehouses and stores. The main challenge is getting reliable data at the right level of detail and ensuring all systems (ERP, Warehouse Management System, Finance) speak the same language.

### Considerations:
- Costs must be captured at a detailed level to enable fair and auditable allocation. This requires strong data discipline and integration across systems.
- Static allocation methods (volume, weight, order count) often fail to capture operational complexity. Rules should evolve with business dynamics.
- Seamless data flow between WMS, TMS, and ERP is key for accurate cost mapping and reconciliation.
- Indirect costs (rent, utilities, IT, admin) must be allocated in a way that is transparent and consistently applied across all units.
- The ability to simulate different allocation models helps improve strategic decisions and resource planning.
- Clear ownership of cost logic and auditability ensures consistency and confidence in reporting.

### Questions
- What is the current level of cost data granularity (per order, per SKU, per warehouse)?
- Are there consistent identifiers to link cost data across ERP, WMS, and TMS?
- Which parts of cost allocation are manual today, and can AI automate or validate them?
- Can we apply anomaly detection to identify abnormal cost patterns?
- Should we use an activity-based costing (ABC) model enhanced by AI?
- How will the allocation model scale with additional warehouses or global expansion?
- Who owns the cost allocation logic (finance vs engineering), and how do we maintain governance with AI in the loop?


### AI/ML Opportunities
AI can play a major role in making cost tracking and allocation more accurate by turning fragmented operational data into actionable insights. In a fulfillment environment, costs come from multiple systems — labor from HR, transport from TMS, and inventory from WMS — and aligning them manually is both slow and error-prone.

AI can help by:

- Identifying true cost drivers — using data patterns to reveal what actually impacts cost (e.g., distance, handling time, or product mix).
- Automating cost attribution — learning from historical allocations and applying rules dynamically as new data comes in.
- Detecting anomalies — flagging unusual cost spikes or mismatches between systems in real time.
- Improving forecasting — predicting future cost behavior based on trends like demand shifts or labor availability.



## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
Cost optimization in fulfillment starts with visibility — understanding where money is actually going. The first step is analyzing key cost drivers such as labor, transportation, and inventory distribution.

I’d start by identifying opportunities through data — combining financial, operational, and system metrics to see where the biggest cost drivers are. That means mapping the full fulfillment flow and quantifying where inefficiencies occur — like underused capacity, unbalanced labor, or high transport variance. I’d also involve Finance, Operations, and HR early, since their data and constraints shape what’s realistically optimizable this means we need cross-system integration. 
For prioritization, I’d use an impact vs. complexity matrix — focusing first on initiatives that offer high savings with manageable technical or process change. For example, improving demand forecasting or transport route planning often yields quick wins. More complex transformations, like warehouse automation or new system integration, would follow once the data foundation and ROI are clear.

I’d take an iterative and data-driven approach for implementation. We can start with pilot projects in one warehouse or flow and then measure outcomes using agreed KPIs (cost per order, fulfillment time, utilization). Feed the result back into the roadmap and scale successful patterns. We also need to support cross-system integration and continuous improvement, so Finance, Operations, and Technology can evolve together rather than in silos.

From an architecture perspective, I’d approach it in layers:

- Define a shared data model and common identifiers things like warehouse IDs, store codes, and cost centers must be consistent across systems to enable traceability.
- Use an API-first or event-driven architecture APIs handle synchronous communication for operational workflows (like creating a shipment), while an event bus (Kafka or similar) handles asynchronous data sharing, such as cost updates or inventory changes.
- Implement a data integration layer either via a data lakehouse or integration platform to aggregate, clean, and standardize data from different systems.
- Enforce governance and validation use schema validation, metadata catalogs, and monitoring to ensure data quality and compliance.
- Expose unified analytics and insights build a central reporting layer (e.g., via a data warehouse or BI tool) so Finance, HR, and Operations see the same truth in real time.

AI can fit into this design as an intelligence layer that sits on top of the integrated systems. Once Finance, HR, and Operations data flow consistently across the architecture, AI can start generating insights and automation rather than just reports.


### Questions
- Which cost categories have the most variability or biggest potential for optimization?
- What are the trade-offs between cost savings and service levels?
- How can we use data analytics to identify inefficiencies or trends that aren’t visible through traditional reports?
- What areas could benefit from automation or process re-engineering?
- How do we ensure cross-departmental alignment when implementing cost-saving measures?
- How can predictive analysis or learning systems assist in identifying future cost risks?

### Considerations
- Integrate data from HR (labor), Procurement (supplier contracts), and Operations (delivery metrics) into a central performance dashboard.
- Build a cost intelligence dashboard to visualize top cost drivers. AI can cluster similar cost behaviors to identify inefficiencies.
- Use AI to predict peak workloads and dynamically schedule labor, reducing idle time and overtime.
- Ensure Finance and Operations share common cost structures and definitions for optimization decisions.
- Use a modular architecture that supports advanced analytics, allowing future optimization tools to plug in easily.
- Optimization should be iterative, with business outcomes feeding back into the system for ongoing improvement.
- Prioritize initiatives that offer measurable value across multiple departments, not just operational savings.
- Collaborate with HR and Operations to handle process changes that may arise from automation or process redesign.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

The integration between cost control tools and financial systems transforms isolated data points into actionable business intelligence. When these systems operate in silos, organizations face delayed reporting, manual reconciliation errors, and missed opportunities for cost optimization. Integration creates a single source of truth that enables real-time financial visibility across the enterprise.

Some of the Key benefits:
- Real-Time Financial Visibility
- Improved Decision-Making Speed
- Better Compliance and Audit Readiness
- Enhanced Accuracy and Reduced Errors

To ensure seemless integeration I'd start with a comprehensive system assessment to understand existing data structures, workflows, and integration points. Choose between API-based integration for real-time synchronization or batch processing for periodic updates based on business requirements. Then by establishing clear data governance policies, create detailed mapping documents showing how data fields correspond between systems. Standardize across all integrated systems. Then begin with a pilot integration covering a single business unit or cost center to validate the approach and identify potential issues. This allows us to refine processes before full deployment. Gradually expand integration scope, adding complexity incrementally while maintaining system stability. We also need a robust Error Handling and Monitoring to catch synchronization failures immediately and establish clear escalation procedures for critical integration failures. After these stages the integeration system needs to be adaopted by the users which they need traning for that. Security and compliance considerations is also need to be applied to the system.


### Questions
- What are the existing financial systems (e.g., SAP, Salesforce, ...) and how flexible are their integration points?
- What are the key data flows between the operational cost tracking and financial reporting systems?
- How do we ensure data consistency and prevent double counting or missing entries?
- What is the best way to handle versioning and updates to financial data models?
- What architectural patterns (e.g., event streaming, ETL pipelines, APIs) best support this integration?
- How can integration enable cross-department collaboration — for instance, Finance forecasting aligning with Operations data?
- What governance structures are required to ensure shared accountability for cost data?

### Considerations
- Design with data integration in mind — a unified data model ensures financial and operational consistency.
- Enable near real-time updates between fulfillment and Finance, improving responsiveness and accuracy.
- Implement data ownership models where Finance validates cost mappings and IT ensures technical compliance.
- Protect sensitive financial data, ensuring encryption, access control, and audit logs are built into the architecture.
- Design integration with fault tolerance and recovery strategies to maintain reliability.
- Cross-department alignment: Allow Finance, Operations, and Supply Chain to access synchronized views of costs through shared analytics platforms.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Budgeting and forecasting in fulfillment operations represent the financial backbone that determines whether a company can meet customer expectations profitably. Unlike other business functions where costs might be relatively stable, fulfillment operations face extreme variability driven by seasonal demand, carrier rate fluctuations, and evolving customer service expectations. This volatility makes sophisticated financial planning not just beneficial but essential for survival in competitive markets.

The foundation of any effective fulfillment budgeting system lies in comprehensive data integration. The system must seamlessly connect with WMS (Warehouse Management Systems), TMS (Transportation Management Systems), order management platforms, and HR systems to capture the full spectrum of operational data. Real-time data feeds ensure forecasts reflect current operational realities rather than outdated assumptions. Historical data storage should maintain at for many years of granular operational metrics, enabling pattern recognition across multiple seasonal cycles and business conditions. This historical depth becomes particularly valuable for identifying subtle trends and correlations that might not be apparent in shorter timeframes.

![This simplified architecture captures the essential flow: Collect → Store → Process → Analyze → Present. Each component can be expanded with additional functionality as the system matures, but these core elements form the foundation of any effective fulfillment budgeting and forecasting system.](<Budgeting-And-Forcating-System-Architecture.png>)

### Questions
- How can Finance, HR, and Operations collaborate on shared forecasting assumptions?
- What level of forecasting accuracy is needed for decision-making?
- How frequently should forecasts be updated or revalidated?
- What external factors (fuel prices, demand surges, inflation) should influence cost projections?
- How can AI-based forecasting be integrated into financial planning workflows?
- How can we use data patterns from historical operations to forecast future costs more effectively?
- Should forecasting models be centralized or tailored per business unit?
- How can we identify when forecast assumptions start to deviate from actual performance?

### Considerations
- Use AI/ML forecasting models (time series, regression) for labor, transportation, and inventory cost projections.
- Support best-case, expected, and worst-case projections for more resilient decision-making.
- Incorporate external data — fuel prices, market demand, inflation — into predictive models.
- AI can automatically flag budget overruns or forecast deviations.
- Ensure finance, supply chain, and operations collaborate on assumptions and models.
- AI forecasts must remain transparent, with clear reasoning for predictions.


## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
Replacing a warehouse represents one of the most complex and risk-laden transitions in supply chain operations. Unlike simple capacity expansions or technology upgrades, a warehouse replacement fundamentally disrupts established cost structures, operational patterns, and performance baselines. Even with identical inventory and similar automation levels, two warehouses rarely exhibit the same cost profile due to differences in layout, location, labor markets, and countless operational nuances. 
Historical cost data from the existing warehouse provides the only reliable benchmark for evaluating new facility performance. This data reveals the full range of operational scenarios.   

### Importance
- Preserving cost history allows benchmarking and trend analysis between old and new facilities.
- Historical data provides a cost baseline for evaluating operational improvements.
- Ensure historical data is archived safely but remains queryable for analysis.
- Use AI to identify patterns in old warehouse inefficiencies that can be avoided in the new one.
- Maintain linkage between archived data and the new warehouse identifier for audit and comparison.

### Questions
- How will historical cost data be archived and accessed when needed?
- What KPIs will be used to compare the performance of the old and new warehouses?
- How do we ensure continuity in reporting when reusing Business Unit Codes?
- What lessons from the old warehouse’s cost patterns should influence planning for the new one?
- How can data-driven insights help in setting realistic operational budgets for the new facility?
- Who is responsible for validating data integrity during the transition?

### Considerations
- Preserve archived data in a structured, queryable format accessible by Finance or other parties that benefit from this.
- Use legacy performance data to set KPIs and cost expectations for the new facility.
- Link new warehouse identifiers to archived data to maintain end-to-end auditability.
- Ensure the accounting system recognizes the transition without breaking ledger continuity.
- Collaborate across departments to learn from past inefficiencies and inform process improvements.
- Design the data and cost control architecture to handle similar transitions smoothly as the company scales.


## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
