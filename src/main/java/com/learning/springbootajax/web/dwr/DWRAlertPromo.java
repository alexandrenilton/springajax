package com.learning.springbootajax.web.dwr;

import com.learning.springbootajax.repository.PromocaoRepository;
import org.directwebremoting.Browser;
import org.directwebremoting.ScriptSessions;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Component
@RemoteProxy
public class DWRAlertPromo {
    private static final Logger log = LoggerFactory.getLogger(DWRAlertPromo.class);


    @Autowired
    private PromocaoRepository repository;

    private LocalDateTime getLastDataOffer() {
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.Direction.DESC, "dtCadastro");
        return repository.findLastPromotionDate(pageRequest)
                .getContent() // retorna java.util.List
                .get(0);
    }

    @RemoteMethod // vai ser o metodo init lá no promo-list.js
    public synchronized void init() {
        System.out.println("init invoked!");
        LocalDateTime lastDate = getLastDataOffer();

        WebContext context = WebContextFactory.get();


        // agendamento de tarefas...
        Timer timer = new Timer();
        timer.schedule(new AlertTask(context, lastDate), 10000, 60000);
    }

    class AlertTask extends TimerTask {
        private LocalDateTime lastDate;
        private WebContext context;
        private long count;

        public AlertTask(WebContext context, LocalDateTime lastDate) {
            super();
            this.lastDate = lastDate;
            this.context = context;
        }

        @Override //thread da DWR
        public void run() {

            String session = context.getScriptSession().getId();

            Browser.withSession(context, session, new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> map = repository.totalAndLastPromoByDataCadastro(lastDate); // lastData tá na superclasse
                    count = (Long) map.get("count");
                    lastDate =
                            map.get("lastDate") == null ? lastDate : (LocalDateTime) map.get("lastDate");

                    Calendar time = Calendar.getInstance();
                    time.setTimeInMillis(context.getScriptSession().getLastAccessedTime());
                    System.out.println("count: " + count
                        + ", lastDate: " + lastDate
                        + "<" + session + "> " + " <" + time.getTime() + "<");

                    if (count > 0) {
                        // showButton é funçao lá no JS e count é o parametro dela
                        ScriptSessions.addFunctionCall("showButton", count);
                    }
                }
            });
        }
    }
}
