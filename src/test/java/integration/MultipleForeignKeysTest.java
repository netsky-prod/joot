package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Message;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.MESSAGE;
import static io.github.jtestkit.joot.test.fixtures.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for tables with multiple foreign keys pointing to the same parent table.
 * <p>
 * Uses the `message` table with `sender_id` and `receiver_id` both referencing `users`.
 * <p>
 * Demonstrates three scenarios:
 * 1. Both FKs auto-created → two different parent entities
 * 2. One FK explicit, one auto-created → one explicit, one new parent
 * 3. Both FKs explicit → both use specified values
 */
class MultipleForeignKeysTest extends BaseIntegrationTest {

    private JootContext ctx;

    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }

    @Test
    void shouldCreateTwoDifferentUsersWhenBothFKsAutoCreated() {
        // ACT: Create a message without specifying sender or receiver
        // Use generateNullables(false) to avoid cascade FK creation (users → team → users)
        Message message = ctx.create(MESSAGE, Message.class)
            .generateNullables(false)
            .build();

        // ASSERT: Two different users were auto-created
        assertThat(message.getSenderId()).isNotNull();
        assertThat(message.getReceiverId()).isNotNull();
        assertThat(message.getSenderId()).isNotEqualTo(message.getReceiverId());

        // Both users should exist in database
        assertThat(dsl.fetchCount(USERS)).isEqualTo(2);

        // Verify we can fetch both users
        Users sender = ctx.get(message.getSenderId(), USERS, Users.class);
        Users receiver = ctx.get(message.getReceiverId(), USERS, Users.class);
        
        assertThat(sender).isNotNull();
        assertThat(receiver).isNotNull();
        assertThat(sender.getId()).isNotEqualTo(receiver.getId());
    }

    @Test
    void shouldAllowOneFKExplicitAndOneAutoCreated() {
        // ARRANGE: Create sender explicitly
        Users sender = ctx.create(USERS, Users.class)
            .set(USERS.USERNAME, "Alice")
            .set(USERS.EMAIL, "alice@example.com")
            .generateNullables(false)  // Avoid cascade FK
            .build();

        // ACT: Create message with explicit sender, auto-created receiver
        Message message = ctx.create(MESSAGE, Message.class)
            .set(MESSAGE.SENDER_ID, sender.getId())
            // receiver_id will be auto-created
            .generateNullables(false)  // Avoid cascade FK for auto-created receiver
            .build();

        // ASSERT: Sender is the explicit one, receiver is auto-created
        assertThat(message.getSenderId()).isEqualTo(sender.getId());
        assertThat(message.getReceiverId()).isNotNull();
        assertThat(message.getReceiverId()).isNotEqualTo(sender.getId());

        // Two users in database: explicit sender + auto-created receiver
        assertThat(dsl.fetchCount(USERS)).isEqualTo(2);

        // Verify the explicit sender preserved its data
        Users fetchedSender = ctx.get(message.getSenderId(), USERS, Users.class);
        assertThat(fetchedSender.getUsername()).isEqualTo("Alice");
        assertThat(fetchedSender.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldAllowBothFKsToBeExplicitlySet() {
        // ARRANGE: Create two users explicitly
        Users alice = ctx.create(USERS, Users.class)
            .set(USERS.USERNAME, "Alice")
            .generateNullables(false)
            .build();
        
        Users bob = ctx.create(USERS, Users.class)
            .set(USERS.USERNAME, "Bob")
            .generateNullables(false)
            .build();

        // ACT: Create message with both FKs explicit (no auto-creation)
        Message message = ctx.create(MESSAGE, Message.class)
            .set(MESSAGE.SENDER_ID, alice.getId())
            .set(MESSAGE.RECEIVER_ID, bob.getId())
            .build();
        
        // ASSERT: Both FKs use the explicit values
        assertThat(message.getSenderId()).isEqualTo(alice.getId());
        assertThat(message.getReceiverId()).isEqualTo(bob.getId());

        // Only two users in database (no auto-creation)
        assertThat(dsl.fetchCount(USERS)).isEqualTo(2);
    }

    @Test
    void shouldAllowBothFKsPointingToSameUser() {
        // ARRANGE: Create one user (for self-messaging scenario)
        Users user = ctx.create(USERS, Users.class)
            .set(USERS.USERNAME, "SelfMessager")
            .generateNullables(false)
            .build();

        // ACT: Create message where sender and receiver are the same person
        Message message = ctx.create(MESSAGE, Message.class)
            .set(MESSAGE.SENDER_ID, user.getId())
            .set(MESSAGE.RECEIVER_ID, user.getId())  // Same user!
            .set(MESSAGE.CONTENT, "Note to self")
            .build();
        
        // ASSERT: Both FKs point to the same user
        assertThat(message.getSenderId()).isEqualTo(message.getReceiverId());
        assertThat(message.getSenderId()).isEqualTo(user.getId());

        // Only one user in database
        assertThat(dsl.fetchCount(USERS)).isEqualTo(1);
    }

    @Test
    void shouldCreateMultipleMessagesWithDifferentAutoCreatedUsers() {
        // ACT: Create multiple messages, each auto-creates its own sender/receiver
        Message msg1 = ctx.create(MESSAGE, Message.class)
            .generateNullables(false)
            .build();
        Message msg2 = ctx.create(MESSAGE, Message.class)
            .generateNullables(false)
            .build();
        Message msg3 = ctx.create(MESSAGE, Message.class)
            .generateNullables(false)
            .build();

        // ASSERT: Each message has different users
        assertThat(msg1.getSenderId()).isNotEqualTo(msg2.getSenderId());
        assertThat(msg1.getReceiverId()).isNotEqualTo(msg2.getReceiverId());
        assertThat(msg2.getSenderId()).isNotEqualTo(msg3.getSenderId());

        // Total: 3 messages × 2 users = 6 users
        assertThat(dsl.fetchCount(USERS)).isEqualTo(6);
        assertThat(dsl.fetchCount(MESSAGE)).isEqualTo(3);
    }
}

