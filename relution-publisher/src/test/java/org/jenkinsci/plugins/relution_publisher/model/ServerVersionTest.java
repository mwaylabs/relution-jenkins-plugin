
package org.jenkinsci.plugins.relution_publisher.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class ServerVersionTest {

    @Test
    public void majorShouldBeEqual() {
        final ServerVersion lhs = new ServerVersion("1.0");
        final ServerVersion rhs = new ServerVersion("1.0");

        assertThat(lhs).isEqualTo(rhs);
    }

    @Test
    public void majorShouldNotBeEqual() {
        final ServerVersion lhs = new ServerVersion("1.0");
        final ServerVersion rhs = new ServerVersion("2.0");

        assertThat(lhs).isNotEqualTo(rhs);
    }

    @Test
    public void majorShouldBeLess() {
        final ServerVersion lhs = new ServerVersion("1.0");
        final ServerVersion rhs = new ServerVersion("2.0");

        assertThat(lhs).isLessThan(rhs);
    }

    @Test
    public void majorShouldBeGreater() {
        final ServerVersion lhs = new ServerVersion("2.0");
        final ServerVersion rhs = new ServerVersion("1.0");

        assertThat(lhs).isGreaterThan(rhs);
    }

    @Test
    public void majorShouldBeSame() {
        final ServerVersion lhs = new ServerVersion("2.0");
        final ServerVersion rhs = new ServerVersion("2.0");

        assertThat(lhs).isGreaterThanOrEqualTo(rhs);
        assertThat(rhs).isLessThanOrEqualTo(rhs);
    }

    @Test
    public void minorShouldBeEqual() {
        final ServerVersion lhs = new ServerVersion("1.1");
        final ServerVersion rhs = new ServerVersion("1.1");

        assertThat(lhs).isEqualTo(rhs);
    }

    @Test
    public void minorShouldNotBeEqual() {
        final ServerVersion lhs = new ServerVersion("1.0");
        final ServerVersion rhs = new ServerVersion("1.1");

        assertThat(lhs).isNotEqualTo(rhs);
    }

    @Test
    public void minorShouldBeLess() {
        final ServerVersion lhs = new ServerVersion("1.0");
        final ServerVersion rhs = new ServerVersion("1.1");

        assertThat(lhs).isLessThan(rhs);
    }

    @Test
    public void minorShouldBeGreater() {
        final ServerVersion lhs = new ServerVersion("1.1");
        final ServerVersion rhs = new ServerVersion("1.0");

        assertThat(lhs).isGreaterThan(rhs);
    }

    @Test
    public void minorShouldBeSame() {
        final ServerVersion lhs = new ServerVersion("1.1");
        final ServerVersion rhs = new ServerVersion("1.1");

        assertThat(lhs).isGreaterThanOrEqualTo(rhs);
        assertThat(rhs).isLessThanOrEqualTo(rhs);
    }

    @Test
    public void v2_7_4_shouldBeLessThan_v3_36() {
        final ServerVersion lhs = new ServerVersion("2.7.4");
        final ServerVersion rhs = new ServerVersion("3.36");

        assertThat(lhs).isNotEqualTo(rhs);
        assertThat(lhs).isLessThan(rhs);
        assertThat(rhs).isGreaterThan(lhs);
    }

    @Test
    public void v3_40_shouldBeGreaterThan_v3_36() {
        final ServerVersion lhs = new ServerVersion("3.40");
        final ServerVersion rhs = new ServerVersion("3.36");

        assertThat(lhs).isNotEqualTo(rhs);
        assertThat(lhs).isGreaterThan(rhs);
        assertThat(rhs).isLessThan(lhs);
    }

    @Test
    public void nullShouldBeLess() {
        final ServerVersion lhs = new ServerVersion(null);
        final ServerVersion rhs = new ServerVersion("1.0");

        assertThat(lhs).isLessThan(rhs);
    }

    @Test
    public void unknownShouldBeGreater() {
        final ServerVersion lhs = new ServerVersion("693091cae45967c27e95b7d4985ef16c24edd65c@origin/master");
        final ServerVersion rhs = new ServerVersion("1.0");

        assertThat(lhs).isGreaterThan(rhs);
    }
}
