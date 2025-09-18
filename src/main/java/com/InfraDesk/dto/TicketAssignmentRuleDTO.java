package com.InfraDesk.dto;

import com.InfraDesk.entity.TicketAssignmentRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssignmentRuleDTO {

    private Long id;

    private String companyId;
    private String companyName;

    private String departmentId;
    private String departmentName;

    private String locationId;
    private String locationName;

    private String ticketTypeId; // optional
    private String ticketTypeName;

    private Set<String> assigneeUserIds;
    private Set<String> assigneeUserNames;

    private Set<String> assigneeGroupIds;
    private Set<String> assigneeGroupNames;

    private String assigneeRole; // optional role-based assignment

    private Integer priority;

}
