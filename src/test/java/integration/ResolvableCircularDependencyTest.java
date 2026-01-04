package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Team;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.TEAM;
import static io.github.jtestkit.joot.test.fixtures.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD tests for resolvable circular dependencies between tables.
 * 
 * Schema:
 * - users.default_team_id → team (NULLABLE)
 * - team.owner_user_id → users (NOT NULL)
 * 
 * This cycle is resolvable via two-phase INSERT:
 * 1. INSERT users (default_team_id = NULL)
 * 2. INSERT team (owner_user_id = users.id)
 * 3. UPDATE users SET default_team_id = team.id
 */
class ResolvableCircularDependencyTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldBreakCycleByLeavingNullableFKAsNull() {
        // ACT: Create Team (has NOT NULL FK to Users)
        // Users has nullable FK to Team → creates circular dependency
        // Joot breaks cycle by leaving users.default_team_id as NULL
        Team team = ctx.create(TEAM, Team.class).build();
        
        // ASSERT: Team created successfully
        assertThat(team).isNotNull();
        assertThat(team.getId()).isNotNull();
        assertThat(team.getOwnerUserId()).isNotNull();
        
        // ASSERT: Owner user was created
        Users owner = dsl.selectFrom(USERS)
            .where(USERS.ID.eq(team.getOwnerUserId()))
            .fetchOneInto(Users.class);
        
        assertThat(owner).isNotNull();
        
        // ASSERT: Owner's default_team_id is NULL (cycle broken!)
        // This happens regardless of generateNullables setting
        assertThat(owner.getDefaultTeamId()).isNull();
        
        // Both entities exist in DB
        assertThat(dsl.fetchCount(USERS)).isEqualTo(1);
        assertThat(dsl.fetchCount(TEAM)).isEqualTo(1);
    }
    
    @Test
    void shouldIgnoreGenerateNullablesForCyclicFK() {
        // ACT: Create Team with generateNullables=true (default)
        // Cyclic FK (users.default_team_id) should be NULL regardless
        Team team = ctx.create(TEAM, Team.class)
            .generateNullables(true)  // Explicitly true
            .build();
        
        // ASSERT: Team created
        assertThat(team.getId()).isNotNull();
        assertThat(team.getOwnerUserId()).isNotNull();
        
        // ASSERT: Owner user created
        Users owner = dsl.selectFrom(USERS)
            .where(USERS.ID.eq(team.getOwnerUserId()))
            .fetchOneInto(Users.class);
        
        assertThat(owner).isNotNull();
        
        // ASSERT: Owner's default_team_id is NULL (cyclic FK ignores generateNullables)
        assertThat(owner.getDefaultTeamId()).isNull();
        
        // Both entities exist
        assertThat(dsl.fetchCount(USERS)).isEqualTo(1);
        assertThat(dsl.fetchCount(TEAM)).isEqualTo(1);
    }
    
    @Test
    void shouldAllowExplicitFKToBreakCycle() {
        // ARRANGE: Create user explicitly with generateNullables=false to avoid creating Team
        Users user = ctx.create(USERS, Users.class)
            .set(USERS.USERNAME, "explicit-user")
            .generateNullables(false)  // Don't auto-create team for default_team_id
            .build();
        
        // ACT: Create team with explicit owner (breaks cycle)
        Team team = ctx.create(TEAM, Team.class)
            .set(TEAM.OWNER_USER_ID, user.getId())
            .build();
        
        // ASSERT: Team uses explicit user
        assertThat(team.getOwnerUserId()).isEqualTo(user.getId());
        
        // Only one user (no auto-created user)
        assertThat(dsl.fetchCount(USERS)).isEqualTo(1);
        
        // Only one team (no auto-created team)
        assertThat(dsl.fetchCount(TEAM)).isEqualTo(1);
    }
}

