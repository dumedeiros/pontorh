#{extends 'securesocial/SecureSocial/main.html'/}
#{set title: messages.get('securesocial.changeTitle') /}

#{field 'newPassword'}
<div class="clearfix #{ifError field.name} error #{/ifError}">
    <label for="${field.name}">&{'securesocial.newPassword'}</label>

    <div class="input">
        <input id="${field.id}" class="large" name="${field.name}" type="password"/>
        #{ifError field.name}
            <span class="help-inline">${field.error}</span>
        #{/ifError}
    </div>
</div>
#{/field}

#{field 'confirmPassword'}
<div class="clearfix #{ifError field.name} error #{/ifError}">
    <label for="confirmPassword">&{'securesocial.confirmPassword'}</label>

    <div class="input">
        <input id="${field.id}" class="large" name="${field.name}" type="password"/>
        #{ifError field.name}
            <span class="help-inline">${field.error}</span>
        #{/ifError}
    </div>
</div>
#{/field}

<div class="actions">
    <input type="submit" value="Submit" class="btn primary">
    <a href="@{securesocial.SecureSocial.login()}" class="btn">&{'securesocial.cancel'}</input></a>
</div>
