# Handover Readiness Score (0–100) — Special Standout Innovation

## 1. Problem Statement

In corporate engineering and knowledge environments, employee handovers are often rushed, unstructured, and subjective. Departing employees leave behind unassigned tasks, undocumented code, mysterious production access secrets, and untraceable decisions. Incoming employees struggle for months trying to reconstruct what happened and what to do next.

**HandoffIQ introduces the Handover Readiness Score**: an objective, transparent, algorithmic evaluation engine that scores the quality and completeness of an employee transition from **0 to 100**, and **actively blocks handover finalization** until critical criteria are satisfied.

---

## 2. Mathematical Formulation & Weight Distribution

The Handover Readiness Score is computed by `ReadinessScorerService` using a weighted sum across 7 rigorous operational dimensions:

$$\text{Readiness Score} = \sum_{i=1}^{7} (W_i \times C_i)$$

Where $W_i$ is the maximum weight percentage and $C_i \in [0.0, 1.0]$ is the completion ratio of criterion $i$:

| Index | Criterion | Weight ($W_i$) | Mandatory Gate? | Evaluation Logic |
|---|---|---|---|---|
| **C1** | **All Pending Tasks Have Designated New Owners** | **20%** | **Yes** | Computes ratio of pending tasks transitioned from the departing employee that have an assigned receiving employee. If unassigned pending tasks remain, score is reduced proportionally. |
| **C2** | **In-Progress Deliverables Have Status Notes** | **15%** | **Yes** | Verifies that every task currently marked `IN_PROGRESS` contains at least one recent comment or handover transition remark explaining current state and blockers. |
| **C3** | **Completed Deliverables Have Documented Outcomes** | **15%** | No | Checks that tasks marked `COMPLETED` under this handover have verifiable outcome descriptions (e.g. "PR merged", "Documentation prepared"). |
| **C4** | **Critical Blockers & Risks Are Documented** | **15%** | No | Evaluates whether any `BLOCKED` or `CRITICAL` priority items contain an explicit risk mitigation plan or escalation instruction. |
| **C5** | **Relevant Runbooks & Knowledge Docs Attached** | **10%** | No | Checks that at least one architecture document, credential guide, or runbook is attached to the handover package. |
| **C6** | **Key Decisions Referenced & Linked** | **15%** | No | Validates that recent project decisions from associated meetings are linked to provide historical rationale for incoming teammates. |
| **C7** | **Receiving Employee Has Formally Acknowledged** | **10%** | **Yes** | Confirms whether the incoming employee has reviewed the package and clicked "Acknowledge Receipt" in the portal. |
| **Total**| **Composite Readiness Score** | **100%** | — | Evaluated in real-time or on-demand. |

---

## 3. Readiness Score Tiers & Visual Indicators

```
  0% ---------------- 50% ----------------- 75% ---------------- 100%
  [  Tier 1: Critical  ] [  Tier 2: In Progress ] [    Tier 3: Ready     ]
       Red Meter               Amber Meter             Green Meter
```

### Tier 1: Incomplete / Critical Risk (0% – 49%)
- **Status Indicator**: Red (`#EF4444`)
- **System Action**: Completion is strictly locked. The UI presents a detailed missing requirements alert banner.
- **Typical Reasons**: Tasks remain unassigned, no documentation attached, receiving employee has not reviewed.

### Tier 2: In Progress / Substantial Information (50% – 74%)
- **Status Indicator**: Amber (`#F59E0B`)
- **System Action**: Approaching readiness. Outstanding items (such as formal receiver acknowledgement or blocker documentation) must be resolved.
- **Typical Reasons**: Tasks have assignees, but formal review or outcome documentation is incomplete.

### Tier 3: Ready for Completion (75% – 100%)
- **Status Indicator**: Emerald Green (`#10B981`)
- **System Action**: Finalize & Complete Handover button becomes enabled.
- **Pre-requisite**: All mandatory gates (New owners assigned, in-progress notes present, receiver acknowledgement) must be satisfied.

---

## 4. Enforcement Guard: Handover Completion Prevention

Unlike traditional project management tools that allow closing records arbitrarily, HandoffIQ enforces data integrity at the backend level:

```java
// HandoverService.java
public HandoverResponse completeHandover(Long handoverId) {
    Handover handover = findOrThrow(handoverId);
    HandoverReadinessReport report = readinessScorerService.evaluateHandover(handover);

    if (report.getScore() < 75 || !report.isReadyForCompletion()) {
        throw new HandoverIncompleteException(
            String.format("Cannot complete handover #%d: Current Readiness Score is %d%% (minimum 75%% required). " +
                          "Missing requirements: %s",
                          handoverId, report.getScore(), String.join("; ", report.getMissingRequirements()))
        );
    }

    handover.setStatus(HandoverStatus.COMPLETED);
    handover.setCompletedAt(LocalDateTime.now());
    return mapToResponse(handoverRepository.save(handover));
}
```

If an unauthorized or premature attempt to close a handover occurs:
1. An HTTP 400 Bad Request is returned with error code `HANDOVER_INCOMPLETE`.
2. The exact missing requirements are listed in the response payload.
3. The frontend displays an alert highlighting the specific missing items.
4. An audit log entry is saved under `activity_logs`.

---

## 5. UI Presentation & Interactive Elements

1. **`ReadinessScoreMeter.jsx`**:
   - An SVG circular gauge with dynamic stroke-dashoffset animation.
   - Real-time score number and descriptive tier badge.
   - Dynamic status caption explaining eligibility.
2. **`ReadinessChecklistView.jsx`**:
   - 7 criteria cards showing individual weights ($15\%$, $20\%$, etc.), status badge (`Passed` vs `Pending`), and specific guidance.
   - Red callout box listing all unmet requirements.
3. **Recalculate On Demand**:
   - A dedicated "Recalculate Score" button allows users to immediately see score improvements after updating tasks, adding notes, or acknowledging receipt.
