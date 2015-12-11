package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HSODevsPage extends HSOUserManagement {
    public HSODevsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public WebElement getUserRow(User user) {
        return getUserRowByUsername(user.getUsername());
    }

    @Override
    public void deleteUser(String userName) {
        throw new InvalidActionException("deleteUser");
    }

    @Override
    public void setRoleValueFor(User user, Role newRole) {
        throw new InvalidActionException("setRoleValueFor");
    }

    private class InvalidActionException extends RuntimeException {
        public InvalidActionException(String reason){
            super("Cannot perform " + reason + "() on a developer" );
        }
    }
}
