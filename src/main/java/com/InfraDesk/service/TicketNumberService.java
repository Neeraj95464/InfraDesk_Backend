//package com.InfraDesk.service;
//
//import com.InfraDesk.entity.*;
//import com.InfraDesk.repository.TicketCounterRepository;
//import jakarta.persistence.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.dao.DataIntegrityViolationException;
//
//@Service
//public class TicketNumberService {
//    private final EntityManager em;
//    private final TicketCounterRepository counterRepository;
//
//    public TicketNumberService(EntityManager em, TicketCounterRepository counterRepository) {
//        this.em = em;
//        this.counterRepository = counterRepository;
//    }
//
//    /**
//     * Returns next sequence (1-based) for the given company and optional department.
//     * Uses SELECT ... FOR UPDATE style locking to guarantee sequential increments.
//     */
//    @Transactional
//    public long nextSeq(String companyId, String departmentId) {
//        // We'll attempt a few retries in case of concurrent insert races.
//        int maxRetries = 5;
//        for (int attempt = 0; attempt < maxRetries; attempt++) {
//            try {
//                // Build native SQL: lock the row for update
//                String sql;
//                Query q;
//                if (departmentId == null) {
//                    sql = "SELECT id, last_seq FROM ticket_counters WHERE company_id = :companyId AND department_id IS NULL FOR UPDATE";
//                    q = em.createNativeQuery(sql);
//                    q.setParameter("companyId", companyId);
//                } else {
//                    sql = "SELECT id, last_seq FROM ticket_counters WHERE company_id = :companyId AND department_id = :deptId FOR UPDATE";
//                    q = em.createNativeQuery(sql);
//                    q.setParameter("companyId", companyId);
//                    q.setParameter("deptId", departmentId);
//                }
//
//                @SuppressWarnings("unchecked")
//                java.util.List<Object[]> rows = q.getResultList();
//                if (rows.isEmpty()) {
//                    // Insert initial counter row with last_seq = 1
//                    TicketCounter newCounter = TicketCounter.builder()
//                            .company(em.getReference(Company.class, companyId))
//                            .department(departmentId == null ? null : em.getReference(Department.class, departmentId))
//                            .lastSeq(1L)
//                            .build();
//                    try {
//                        counterRepository.saveAndFlush(newCounter);
//                        return 1L;
//                    } catch (DataIntegrityViolationException ex) {
//                        // race during insert -> retry
//                        Thread.yield();
//                        continue;
//                    }
//                } else {
//                    Object[] row = rows.get(0);
//                    Number idNum = (Number) row[0];
//                    Number lastSeqNum = (Number) row[1];
//                    Long id = idNum.longValue();
//                    Long lastSeq = lastSeqNum.longValue();
//                    Long next = lastSeq + 1;
//                    // Update row
//                    TicketCounter counter = em.find(TicketCounter.class, id, LockModeType.PESSIMISTIC_WRITE);
//                    counter.setLastSeq(next);
//                    em.merge(counter);
//                    em.flush();
//                    return next;
//                }
//            } catch (PersistenceException pe) {
//                // transient DB error - retry
//                if (attempt == maxRetries - 1) throw pe;
//            }
//        }
//        throw new IllegalStateException("Failed to allocate ticket sequence (too many retries)");
//    }
//}


package com.InfraDesk.service;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.TicketCounter;
import com.InfraDesk.repository.TicketCounterRepository;
import jakarta.persistence.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketNumberService {

    private final EntityManager em;
    private final TicketCounterRepository counterRepository;

    public TicketNumberService(EntityManager em, TicketCounterRepository counterRepository) {
        this.em = em;
        this.counterRepository = counterRepository;
    }

    /**
     * Returns the next ticket sequence number (per company & optional department).
     * - Uses FOR UPDATE row locking for concurrency safety
     * - Falls back to insert if no counter exists
     *
     * @param companyPublicId   company identifier (publicId)
     * @param departmentPublicId department identifier (publicId) - may be null
     * @return next sequence number
     */

    @Transactional
    public long nextSeq(String companyPublicId, String departmentPublicId) {
        Company company = em.createQuery(
                        "SELECT c FROM Company c WHERE c.publicId = :pid", Company.class)
                .setParameter("pid", companyPublicId)
                .getSingleResult();

        Department department;
        if (departmentPublicId != null) {
            department = em.createQuery(
                            "SELECT d FROM Department d JOIN d.company c " +
                                    "WHERE d.publicId = :dpid AND c.publicId = :cpid",
                            Department.class)
                    .setParameter("dpid", departmentPublicId)
                    .setParameter("cpid", companyPublicId)
                    .getSingleResult();
        } else {
            department = null;
        }

        // Lock row if exists
        TicketCounter counter = counterRepository
                .findForUpdate(company, department)
                .orElseGet(() -> {
                    // Create new counter
                    TicketCounter c = TicketCounter.builder()
                            .company(company)
                            .department(department)
                            .lastSeq(0L)
                            .build();
                    return counterRepository.saveAndFlush(c);
                });

        long next = counter.getLastSeq() + 1;
        counter.setLastSeq(next);
        counterRepository.saveAndFlush(counter);
        return next;
    }

}


