package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.securesocial.SecureSocial;
import models.pontorh.Avatar;
import models.pontorh.Period;
import models.pontorh.User;
import models.pontorh.WorkDay;
import models.securesocial.Account;
import models.securesocial.LinkedAccount;
import play.Play;
import play.data.binding.As;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Scope;
import play.mvc.With;
import securesocial.provider.IdentityProvider;
import securesocial.provider.ProviderRegistry;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import security.LinkedAccountToSocialUser;
import utils.PontoRHUtils;
import utils.ReflectionUtils;
import utils.StringUtils;

import java.util.*;

@With({SecureSocial.class,
        Deadbolt.class})
public class Application extends RootController {
    private static final String ORIGINAL_URL = "originalUrl";
    private static final String ACCOUNT_URL = "/application/account";
    private static final String SUCCESS = "success";

    //TODO Em Application.java colocar o checkAutenticyToken em um @Before method e executar antes de todos os metodos que são
    // requisitados de um #form

    //TODO substituir todos os <form> por #form para possuir os metodos #{authenticityToken /}
    //ou colocar #{authenticityToken /} em todos os <form>s

    @Before(unless = {}, priority = Integer.MAX_VALUE)
    static void globals() {


        User loggedUser;

        if (!request.isAjax()) {
            //Obtem a ultima conta de rede social setada nos cookies
            SocialUser socialUser = SecureSocial.getCurrentUser();

            //Encontra o linkedAccount referente à rede social no db
            LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(socialUser.id.provider,
                    socialUser.id.id);

            //Se nao existe um userName na Sessao (cookies=userId) dessa camada
            // (application e nao secureSocial -> secureSocial usa outros cookies diferentes
            // dos da camada "application")
            //Ou seja, se o User nem a Account ainda nao foi criado, pois caso contrario ele já estaria carregado
            if (session.get("userId") == null) {
                //Se tem uma linkedAccount e um usuario no contexto da applicacao,
                //entao reforça a "setagem" do usuario nos cookies -> userId
                if (linkedAccount != null
                        && linkedAccount.user != null) {
                    setLoggedUser(linkedAccount.user.id);
                }
                //se a linkedAccount em questao ainda não foi vinculada a um usuário
                // então vincule!
                else {
                    createAccount();
                }
            } else {
                //Se o usuario ta na sessao (userId), obter Usuario - DB
                loggedUser = getLoggedUser();
                //Se o usuario, ainda assim não for encontrado
                //(pode ter sido apagado nesse interim)
                if (loggedUser == null) {
                    //Vincula linkedaccount ao usuario
                    createAccount();
                }
            }

            /** ↑↑↑↑ A vinculação da linkedAccount ao User na etapa anterior se refere à primeira vinculação
             *       (Criação de uma Account e de User)
             *       Ao passar para o createAccount evita passar pelo codigo que tá abaixo, caso contrário, o
             *       linkedAccount seria tratado com uma adição extra e não o primeiro ↑↑↑↑*/

            /** ↓↓↓↓↓ A vinculação da linkedAccount ao User na etapa abaixo se refere às demais vinculações
             *       (Adição de LinkedAccouts) ↓↓↓↓↓*/

            //Obtem usuario logado
            loggedUser = getLoggedUser();


            if (linkedAccount != null) {
                // Se a linkedaccount ainda nao tá vinculado a nenhum usuario, entao Vincule
                if (linkedAccount.user == null) {
                    Account account = getAccount();
                    //Se a Account ainda não possui essa linkedAccount vinculada, vincule!
                    if (!account.linkedAccounts.containsKey(socialUser.id.provider)) {
                        linkedAccount.user = loggedUser;
                        linkedAccount.save();

                        account.linkedAccounts.put(linkedAccount.provider,
                                linkedAccount);
                        account.save();
                        flash.success("Acesso adicionado com sucesso");
                        //Para forçar a remover o flash (não estava desaparecendo
                        flash.discard();
                    }
                } else {//if a linkedaccount já está vinculada a outro usuário
                    if (linkedAccount.user != loggedUser) {
                        //Seta  - na sessão da camada de SecureSocial - no SocialUser qualquer outra LinkedAccount
                        // do usuário logado
                        linkedAccount = LinkedAccount.findAnyByUser(loggedUser);
                        SecureSocial.setUserId(new LinkedAccountToSocialUser().transform(linkedAccount));
                        flash.error("Acesso já vinculado à outra conta.");
                        //Para forçar a remover o flash (não estava desaparecendo
                        flash.discard();
                    }
                }
            }

            renderArgs.put("user", loggedUser);
        }
    }


    private static void checkSession(LinkedAccount linkedAccount) {
        if (session.get("userId") == null) { //Se nao existe um userName nos Sessao (cookies)
            if (linkedAccount != null
                    && linkedAccount.user != null) { //mas o usuario existe e está vinculado a uma linkedAccount
                setLoggedUser(linkedAccount.user.id); //Setar Usuario na sessao
            } else {   //Pode existir um linkedAccount sem um usuario
                // Se fechar o browser, por exemplo?
                createAccount(); //Entao vincule um User ao linkedAccount
                //SecureSocial.login();
            }
        } else {
            User user = getLoggedUser();//Se userName ta na sessao, obter Usuario - DB
            if (user == null) {   //verificar se ele existe de verdade
//                    createAccount();
                SecureSocial.login();
            }
        }
    }

    private static void hasNoEmailRedirect(LinkedAccount linkedAccount) {
        if (linkedAccount != null
                && StringUtils.isEmpty(linkedAccount.email)
                && linkedAccount.user == null) {
            //TODO melhorar isso aqui
            flash.error("Não foi possível resgatar o email vinculado à sua conta do " +
                    linkedAccount.provider.valueName +
                    " Tente-se logar com outra conta abaixo ou obtenha mais informações" +
                    " <a href='ww.'>aqui</a> "
            );

            linkedAccount.delete();
            SecureSocial.login();
        }
    }

    /**
     * Used to do the implementation tests
     */
    public static void teste() {
//        User loggedUser = getLoggedUser();
//        loggedUser.sex = 2;
//        loggedUser.save();
//        index();
    }

    public static void index() {

        //Para Filtrar a data
        String sessionFilterDate = session.get("filterdate");
        session.remove("filterdate");
//        Date filterDate = DateTimeUtils.stringToDate(sessionFilterDate);
        Date filterDate = null;
        if (sessionFilterDate != null) {
            filterDate = new Date();
            filterDate.setTime(new Long(sessionFilterDate));
        }

//        if (sessionFilterDate != null)
//            filterDate.setTime(new Long(sessionFilterDate));

        //To fullfill the date input of date picker in view
        Date date = (filterDate == null || filterDate == null) ? new Date() : filterDate;
        Scope.RenderArgs.current().put("filterDate", filterDate == null ? null : filterDate);
        Scope.RenderArgs.current().put("reportDate", date.getTime());
        Scope.RenderArgs.current().put("today", new Date());


        session.put("lastdate", filterDate != null ? filterDate.getTime() : (new Date()).getTime());

        User user = getLoggedUser();

        //User b = (user != null) ? user : (new User("Eduardo", "dumedeiros", "1234"));

        //b.save();

        List<WorkDay> wds;
        WorkDay p;

        //Get the workdays of today
        //used to calculate the las t time IN
        WorkDay wdDay = WorkDay.getOfDay(new Date(), user);

        //Get the last time IN
        String timeIn = PontoRHUtils.lastTimeIn(wdDay);

        //Get the workdays of the week
        wds = WorkDay.getOfWeek(date, user);
        long wTime = WorkDay.getWdsTime(wds);
        TreeMap wdWeekTree = PontoRHUtils.putInATreeOfWeek(date, wds);


        //Get the workdays of the month
        wds = WorkDay.getOfMonth(date, user);
        long mTime = WorkDay.getWdsTime(wds);
        TreeMap wdMonthTree = PontoRHUtils.putInATreeOfMonth(date, wds);


        render(timeIn, wdMonthTree, wdWeekTree, wTime, mTime);
    }

    public static void monthReport(long dateTime) {

        User user = getLoggedUser();
        Date date = new Date();
        date.setTime(dateTime);
        List<WorkDay> wds = WorkDay.getOfMonth(date, user);
        long mTime = WorkDay.getWdsTime(wds);
        TreeMap wdMonthTree = PontoRHUtils.putInATreeOfMonth(date, wds);
        Date now = new Date();
        render(user, wdMonthTree, mTime, now, date);
    }


    public static void account() {

        Account account = getAccount();
        Map<ProviderType, LinkedAccount> linkedAccounts = account.linkedAccounts;
        LinkedAccount twitter = null;

        Collection<IdentityProvider> identityProviders = ProviderRegistry.all();
        List<ProviderType> providerTypes = new ArrayList<ProviderType>();
        for (IdentityProvider identityProvider : identityProviders) {
            if (!linkedAccounts.containsKey(identityProvider.type)) {
                providerTypes.add(identityProvider.type);
            }
        }

        Avatar userAvatar = Avatar.findByUser(account.user);
        providerTypes.remove(ProviderType.userpass);

        render(linkedAccounts,
                providerTypes,
                twitter,
                userAvatar);
    }

    public static void registerPeriod(String msg) {


        //TODO alterar para user logado
        User loggedUser = getLoggedUser();

        WorkDay wd = WorkDay.getOfDay(new Date(), loggedUser);

        if (wd == null) {
            wd = new WorkDay(new Date(), loggedUser);
            wd.save();
        }
        //TODO talvez trazer os periodos do banco organizados na ordem (usar tree?)
        wd.addMessage(msg);

        Period p;
        if (wd.isDataOutMissing()) {
            p = wd.getLastPeriod();
            p.dateOut = new Date();
        } else {
            p = new Period(new Date(), null, wd);
        }

        p.save();

        flash.success("Registro Efetuado com Sucesso");
        index();
    }

    public static void filterDate(@As("dd/MM/yyyy") Date date) {

        //TODO adicionar o date a algum objeto de contexto e redefinir a funcao index(arg) para index()


        if (date != null) {
            session.put("filterdate", date.getTime());
        }

        index();
    }

    public static void newLinkedAccount(ProviderType provider) {
        User user = getLoggedUser();
        //OBS colocar se a pagina para adicao for diferente de "/" (index)
        //TODO ver uma forma melhor de substituir ACCOUNT_URL como sendo "/application/account. Um outro tipo de redirecionamento
        flash.put(ORIGINAL_URL, ACCOUNT_URL);
        IdentityProvider identityProvider = ProviderRegistry.get(provider);
        //TODO internalization
        SocialUser socialUser = identityProvider.authenticate();
    }

    public static void getUserAvatar() {
        User u = getLoggedUser();
        Avatar avatar = Avatar.findByUser(u);

        if (avatar != null) {
            renderBinary(avatar.toBinary(), avatar.name);
        }
        renderBinary(Play.classloader.getResourceAsStream("resources/avatar-null.png"));
    }

    public static void getUserAvatarAsJson() {
        User u = getLoggedUser();
        Avatar avatar = Avatar.findByUser(u);
        renderJSON(avatar.toBinary());
    }


    public static void unLinkAccount(Long id) {
//
        User user = getLoggedUser();
        List<LinkedAccount> linkedAccounts = LinkedAccount.findByUser(user);

        //Tem pelo menos duas linkeds accounts vinculadas
        if (linkedAccounts.size() > 1) {
            LinkedAccount linkedAccount = LinkedAccount.findById(id);
            Account account = getAccount();
            account.linkedAccounts.remove(linkedAccount.provider);
            linkedAccounts.remove(linkedAccount);
            account.save();
            linkedAccount.delete();

            linkedAccount = linkedAccounts.get(0);
            SocialUser su = new LinkedAccountToSocialUser().transform(linkedAccount);
            SecureSocial.setUserId(su);
            flash.success("Acesso removido com sucesso");

        } else {
            //TODO colocar msg
            flash.error("Não é possível remover a unica forma de se realizar login");
        }

        account();

    }

    public static void editUser() {

        render();
    }

    public static void doEditUser(@Valid User user) throws NoSuchFieldException, IllegalAccessException {

        checkAuthenticity();
        User loggedUser = getLoggedUser();
        if (loggedUser == null) {
            error();
        }

        if (validation.hasErrors()) {
            render("@editUser", user);
        }

        ReflectionUtils.copyAllNonNullFields(loggedUser, user);//Copia todos os novos atributos do form no loggedUser

        loggedUser.save();


        Application.account();
    }


}



    /* static void globals() {
        if (!request.isAjax()) {
            SocialUser socialUser = SecureSocial.getCurrentUser();

            LinkedAccount linkedAccount =
                    LinkedAccount.findByProviderAndUserId(
                            socialUser.id.provider,
                            socialUser.id.id
                    );

            //Se nao existe um userName nos Sessao (cookies)
            if (session.get("userName") == null) {
                if (linkedAccount != null
                        && linkedAccount.user != null) {
                    setLoggedUser(linkedAccount.user.userName);
                }
                //Pode existir um linkedAccount sem um usuario
                else {
                    //Entao adiciin
                    createAccount();
                }
            } else {
                //Se userName ta na sessao, obter Usuario - DB
                User user = getLoggedUser();
                if (user == null) {
                    createAccount();
                }
            }

            if (linkedAccount != null) {
                if (linkedAccount.user != null) {
                    renderArgs.put("user", linkedAccount.user);
                    renderArgs.put("lAcc", linkedAccount);
                    renderArgs.put("displayName",
                            linkedAccount.user.displayName);
                    renderArgs.put("avatarUrl",
                            linkedAccount.user.avatarUrl);
                } else {
                    Account account = getAccount();
                    if (!account.linkedAccounts.containsKey(socialUser.id.provider)) {
                        linkedAccount.user = getLoggedUser();
                        linkedAccount.save();
                        account.linkedAccounts.put(linkedAccount.provider,
                                linkedAccount);
                        account.save();
                    }

                }
            }
        }
    }*/

//@Before(priority = Integer.MAX_VALUE)
//    static void globals() {
//        if (!request.isAjax()) {
//            SocialUser socialUser = SecureSocial.getCurrentUser();
//
//            LinkedAccount linkedAccount =
//                    LinkedAccount.findByProviderAndUserId(
//                            socialUser.id.provider,
//                            socialUser.id.id
//                    );
//
//            hasNoEmailRedirect(linkedAccount); //primeira vinculacao de conta DEVE existir email
//
//            checkSession(linkedAccount);
//
//            if (linkedAccount != null) {
//                if (linkedAccount.user != null) {
//                    renderArgs.put("user", linkedAccount.user);
//                    renderArgs.put("lAcc", linkedAccount);
//                    renderArgs.put("displayName",
//                            linkedAccount.user.displayName);
//                    renderArgs.put("avatarUrl",
//                            linkedAccount.user.avatarUrl);
//                } else {
//                    doNewLinkAccount(linkedAccount, socialUser);
//                }
//            }
//        }
//    }