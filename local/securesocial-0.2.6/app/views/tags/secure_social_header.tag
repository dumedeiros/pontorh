#{extends 'securesocial/SecureSocial/main.html'/}
#{set title: "${_arg}" /}

<div class="span12 columns">
    <div class="page-header">
        <h1>${_arg}</h1>
    </div>

#{if flash.error}
    <div class="alert-message block-message error">
        <div class="alert-actions">
        ${flash.error}
        </div>
    </div>
#{/if}
#{ifErrors}
    <h2>Oops…</h2>
#{/ifErrors}