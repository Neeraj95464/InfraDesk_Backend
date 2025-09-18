//package com.InfraDesk.controller;
//
//import com.InfraDesk.entity.Group;
//import com.InfraDesk.service.GroupService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Set;
//
//@RestController
//@RequestMapping("/api/companies/{companyId}/groups")
//public class GroupController {
//
//    private final GroupService groupService;
//
//    public GroupController(GroupService groupService) {
//        this.groupService = groupService;
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Group>> getAllGroups(@PathVariable String companyId) {
//        return ResponseEntity.ok(groupService.getAllGroups(companyId));
//    }
//
//    @GetMapping("/{groupId}")
//    public ResponseEntity<Group> getGroup(@PathVariable String companyId, @PathVariable Long groupId) {
//        return groupService.getGroup(companyId, groupId)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<Group> createGroup(@PathVariable String companyId,
//                                             @RequestParam String name,
//                                             @RequestParam(required = false) String description,
//                                             @RequestParam(required = false) Set<Long> userIds,
//                                             @RequestParam String createdBy) {
//        Group group = groupService.createGroup(companyId, name, description, userIds, createdBy);
//        return ResponseEntity.ok(group);
//    }
//
//    @PutMapping("/{groupId}")
//    public ResponseEntity<Group> updateGroup(@PathVariable String companyId,
//                                             @PathVariable Long groupId,
//                                             @RequestParam(required = false) String name,
//                                             @RequestParam(required = false) String description,
//                                             @RequestParam(required = false) Set<Long> userIds,
//                                             @RequestParam(required = false) Boolean isActive,
//                                             @RequestParam String updatedBy) {
//        Group group = groupService.updateGroup(companyId, groupId, name, description, userIds, isActive, updatedBy);
//        return ResponseEntity.ok(group);
//    }
//
//    @DeleteMapping("/{groupId}")
//    public ResponseEntity<Void> deleteGroup(@PathVariable String companyId,
//                                            @PathVariable Long groupId,
//                                            @RequestParam String updatedBy) {
//        groupService.deleteGroup(companyId, groupId, updatedBy);
//        return ResponseEntity.noContent().build();
//    }
//}
//

package com.InfraDesk.controller;

import com.InfraDesk.dto.GroupDTO;
import com.InfraDesk.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Fetch all active groups for a company
     */
    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups(@PathVariable String companyId) {
//        System.out.println("Request received "+companyId);
        List<GroupDTO> groups = groupService.getAllGroups(companyId);
//        System.out.println("Sending data "+groups);
        return ResponseEntity.ok(groups);
    }

    /**
     * Fetch a single group by ID
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable String companyId,
                                             @PathVariable Long groupId) {
        GroupDTO group = groupService.getGroup(companyId, groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * Create a new group
     * Accepts JSON body like:
     * {
     *     "name": "IT Team",
     *     "description": "Related to projects",
     *     "userIds": [1,2],
     *     "createdBy": "ithead@mahavirgroup.co"
     * }
     */
    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@PathVariable String companyId,
                                                @RequestBody GroupDTO dto) {
        GroupDTO created = groupService.createGroup(companyId, dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Update an existing group
     * Accepts JSON body like:
     * {
     *     "name": "New Name",
     *     "description": "Updated desc",
     *     "userIds": [1,3],
     *     "isActive": true,
     *     "updatedBy": "admin@company.com"
     * }
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable String companyId,
                                                @PathVariable Long groupId,
                                                @RequestBody GroupDTO dto) {
        GroupDTO updated = groupService.updateGroup(companyId, groupId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft delete a group
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String companyId,
                                            @PathVariable Long groupId,
                                            @RequestParam String updatedBy) {
        groupService.deleteGroup(companyId, groupId, updatedBy);
        return ResponseEntity.noContent().build();
    }
}
