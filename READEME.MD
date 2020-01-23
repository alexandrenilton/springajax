Reverse Ajax
Todos que já trabalharam com requisições Ajax alguma vez na vida, devem ter observado que essas requisições sempre partem do lado cliente para o servidor. Isso porque, requisições Ajax são também baseadas no protocolo HTTP, o qual tem como premissa que todas requisições devem iniciar no lado cliente.

Desta forma, se torna impossível que o servidor, a partir do protocolo HTTP, dê o primeiro passo na comunicação com o cliente, ou seja, ele nunca vai enviar qualquer informação para o cliente sem que o cliente tenha solicitado.

Mas em alguns casos se torna necessário que o servidor envie informações ao cliente em um dado momento, sem que o cliente tenha solicitado por essas informações. Assim, algumas técnicas foram criadas e nomeadas de diferentes formas como Push Server e Reverse Ajax. Com o surgimento do HTML5 e as mudanças que precisaram ser realizadas nos navegadores para esse suporte, foi incluído também um recurso nomeado como WebSocket. Porém, o WebSocket, considerado um protocolo, não trabalha sobre o protocolo HTTP como as requisições Ajax, ele é um protocolo independente baseado em TCP, por isso, não fará parte do nosso estudo.

O que vamos estudar a partir de agora é a técnica de Reverse Ajax. Muitos programadores contestam que exista o conceito de Ajax Reverso, simplesmente porque o lado cliente ainda assim, vai precisar dar o primeiro passo na comunicação com o servidor. Mas para nós, entrar nessa discussão não é importante agora, mas sim como fazer uso desse recurso.

A técnica é baseada em um canal de comunicação que será aberto pelo lado cliente com o servidor. Esse canal fica aberto enquanto a página web estiver aberta. Quando ela for fechada o canal é interrompido e a comunicação será perdida. Durante o tempo que o canal estiver aberto o servidor poderá enviar qualquer informação para o cliente e a qualquer momento. Ao receber essa informação o lado cliente, junto ao JavaScript pode tratá-la e exibi-la ao usuário.

Mas para que esse canal de comunicação seja aberto para a comunicação entre os lados servidor e cliente é necessário o uso de uma biblioteca que gerencie esse processo. No caso do Java a biblioteca que se tornou líder nessa técnica é a Direct Web Remoting (DWR).

DWR is Easy Ajax for Java
A biblioteca DWR gera um JavaScript para permitir que os navegadores invoquem o código Java com a máxima segurança, quase como se ele estivesse sendo executado localmente. É desta forma que o cliente consegue criar o canal de comunicação com servidor, abrindo esse canal entre o navegador e um método baseado em uma classe Java no lado servidor. Vamos ver como isso funciona a partir de agora.

O primeiro passo é incluir na aplicação a biblioteca DWR, para isso, basta acessar a página de downloads da DRW. Como trabalhamos com o Maven, vamos usar a seguinte dependência:
```xml
  <dependency>
    <groupId>org.directwebremoting</groupId>
    <artifactId>dwr</artifactId>
    <version>3.0.2-RELEASE</version>
  </dependency>
```
Após incluir a dependência no projeto, vamos a segundo passo. Devemos criar uma configuração que vincule a DWR ao Spring. Para isso, temos dois processos , onde um deles é criar um arquivo .xml de configuração do tipo Spring. Esse arquivo terá poucas instruções como se pode ver abaixo e será nomeado como dwr-spring.xml. É importante salvá-lo no diretório resources:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:dwr="http://www.directwebremoting.org/schema/spring-dwr"
	xsi:schemaLocation="http://www.springframework.org/schema/beans 
	http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.directwebremoting.org/schema/spring-dwr
    http://www.directwebremoting.org/schema/spring-dwr-3.0.xsd">
 
	<dwr:configuration />    	
	<dwr:annotation-scan 
    	base-package="com.mballem.demoajax.web.dwr"/>    	
	<dwr:annotation-config />    
</beans>
```
<dwr:configuration /> - coloca a DWR como um sendo um bean gerenciado pelo Spring Framework. Seria o mesmo que anotar uma classe com @Configuration;

<dwr:annotation-scan> - esta instrução tem como objetivo informar a DWR o pacote que vai conter as classes que o cliente vai se comunicar com o servidor. para isso, usamos o parâmetro base-package;

<dwr:annotation-config /> - seu objetivo é habilitar o uso de algumas anotações da DWR que podem ser incluídas no processo como uma espécie de configuração. Caso não se use essas anotações, tais configurações devem ser feitas em arquivos .xml, seguindo alguns padrões da DWR.

Para finalizar esta configuração, é importante informar ao Spring Boot sobre o arquivo dwr-spring.xml. Fazemos isso na classe DemoAjaxApplication, incluindo sobre sua assinatura a anotação @ImportResource.

@ImportResource(locations = "classpath:dwr-spring.xml")
@SpringBootApplication
public class DemoAjaxApplication {}
Com o segundo passo concluído, vamos ao terceiro. Nele, precisamos criar um Servlet especifico para a DWR. Embora estejamos trabalhando com o Spring MVC, o qual já tem pelo Spring-Boot um Servlet pré configurado, a DWR Reverse Ajax usa seu próprio Servlet. Sendo assim, na classe principal do projeto vamos adicionar o método que configura este Servlet:
```
@Bean
public ServletRegistrationBean<DwrSpringServlet> dwrSpringServlet() {
	DwrSpringServlet dwrServlet = new DwrSpringServlet();
	
	ServletRegistrationBean<DwrSpringServlet> registrationBean = 
			new ServletRegistrationBean<>(dwrServlet, "/dwr/*");
	
	registrationBean.addInitParameter("debug", "true");
	registrationBean.addInitParameter("activeReverseAjaxEnabled",
									  "true");
	return registrationBean;
}
```
O método dwrSpringServlet() deve ser um bean gerenciado pelo Spring, por isso, temos sobre sua assinatura a anotação @Bean. Como retorno vamos usar a classe ServletRegistrationBean do Spring. Sua finalidade é registrar um novo Servlet na aplicação.
A DWR fornece sua própria classe de servlet para integração com o Spring, que é a DwrSpringServlet. Um objeto dessa classe deve ser atribuído a uma instancia da classe ServletRegistrationBean. É preciso também passar como parâmetro dessai instancia qual o caminho de acesso ao servlet da DWR, que neste caso será /dwr/*. Essa instrução é importante para que o JavaScript mantido no lado cliente consiga encontrar o servlet no lado servidor.
Com ServletRegistrationBean instanciado, é necessário incluir um parâmetro inicial de configuração de servlet que habilita o uso da DWR Reverse Ajax no lado servidor. Para isso, usamos o parâmetro activeReverseAjaxEnabled setado como true. Já o parâmetro de debug é opcional. Quando incluído vai habilitar o debug da DWR junto as operações executadas.

DWR no lado cliente
A partir de agora vamos trabalhar no lado cliente da aplicação. No arquivo promo-list.js devemos incluir uma função responsável por habilitar o Ajax reverso no lado cliente e também abrir o canal de comunicação com o servidor.
```
function init() {
	dwr.engine.setActiveReverseAjax(true);
	dwr.engine.setErrorHandler(error);    	
	DWRAlertaPromocoes.init();
}
 
function error(excpetion) {
	console.log("dwr error: ", excpetion);
}
```
Na função init() o método setActiveReverseAjax(true) vai habilitar o Ajax reverso no lado cliente, bem como usamos uma instrução similar lá na configuração do servlet para habilitar o recurso no lado servidor. Já o método
setErrorHandler(error) é opcional e pode ser usado para a captura de erros durante o processo de comunicação entre servidor e cliente.

O ponto alto aqui é a instrução DWRAlertPromocoes.init(). Esta instrução indica a DWR qual a classe no lado servidor que fará parte do canal de comunicação com o cliente. E nesta classe, temos o método init() que será o método que será uma das extremidades desse canal de comunicação.

A função init() deverá ser executada logo que a página for aberta pelo navegador. Isso é necessário para que o lado cliente comece os trabalhos com a DWR, então, para executa-la podemos usar a instrução abaixo:
```
$(document).ready(function() {
	init();
});
```
DWR no lado servidor
No lado servidor vamos precisar criar uma classe nomeada como DWRAlertPromocoes e nela ter um método init(). A classe em questão será apresentada abaixo:
```java
@Component
@RemoteProxy
public class DWRAlertaPromocoes {    
	@Autowired
	private PromocaoRepository repository;    	
	private Timer timer;
	
	private LocalDateTime getDtCadastroByUltimaPromocao() {
		PageRequest pageRequest = PageRequest.of(0, 1, Direction.DESC, "dtCadastro");
		return repository.findUltimaDataDePromocao(pageRequest)
				.getContent()
				.get(0);
	}
	
	@RemoteMethod
	public synchronized void init() {
		LocalDateTime lastDate = getDtCadastroByUltimaPromocao();    		
		WebContext context = WebContextFactory.get();    		
		timer = new Timer();
		timer.schedule(new AlertTask(context, lastDate), 
		    		   10000, 60000);    		
	}
}
```
Sobre a assinatura da classe temos duas anotações a @Component, que transforma esta classe em um bean do Spring e a @RemoteProxy. Esta última é uma configuração da DWR baseada em anotação como parte do processo da criação do canal de comunicação entre servidor e cliente.
Como variáveis a classe tem a injeção do repositório, para acesso aos métodos da camada de persistência. Temos ainda, uma variável do tipo java.util.Timer, que será usada em um processo de agendamento de tarefas.

Vamos agora analisar o método init(). Ele tem sobre sua assinatura a anotação @RemoteMethod da DWR, a qual é um tipo de configuração entre o método que faz parte de uma das extremidades do canal de comunicação. Assim que o navegador abrir a página e a função init() do JavaScript for executada o processo chega ao método init() da classe DWRAlertPromocoes.

Quando o processo chegar ao método init() o canal estará aberto. A partir de agora o servidor poderá enviar mensagens para o cliente a qualquer momento. Veremos como fazer isso logo mais. Por enquanto vamos entender a lógica do processo desta operação com o Reverse Ajax.

Quando a página que lista as promoções for aberta no navegador, teremos um número X de promoções cadastradas na aplicação e as oito últimas promoções estarão listas lá. A ideia é, quando novas promoções forem cadastradas, o cliente receba um aviso na página a partir de um botão. Este botão vai conter o nº de novas promoções que estão disponíveis para visualização. Assim, quando o usuário clicar no botão estas promoções serão listadas. O Ajax reverso entra no processo para enviar a mensagem sobre as novas promoções, resultando no aparecimento do botão na página com a quantidade de novas promoções.

Ajax Reverso em AçãoAjax Reverso em Ação
Mas no lado servidor será necessário de alguma forma saber qual é a última promoção que está listada no lado cliente. Para saber isso, vamos trabalhar com uma consulta no banco de dados que nos traga a data da promoção mais recente no momento que a página for aberta pelo navegador. Esta consulta é chamada pelo método init() com a instrução getDtCadastroByUltimaPromocao(), atribuindo o resultado a variável lastDate do tipo java.time.LocalDateTime.

Após essa ação vamos atribuir a variável context, do tipo org.directwebremoting.WebContext, o objeto de contexto do canal de comunicação que foi aberto.

A próxima ação é instanciar a variável timer e criar um agendamento de tarefas via método schedule(). O agendamento vai ser usado para que a cada 60 segundos o servidor verifique ser existe uma nova promoção cadastrada, em caso positivo, o lado cliente será informado sobre a quantidade de novas promoções que foram cadastradas no último minuto.

timer = new Timer();
timer.schedule(new AlertTask(context, lastDate), 10000, 60000);
No método schedule() vamos passar três parâmetros. O primeiro é uma instancia da classe AlertTask, a qual vamos criar para executar a tarefa agendada. O segundo é um tempo de delay para que a tarefa seja executada pela primeira vez. Esse valor é representado em milissegundos, onde cada 1000 milissegundos equivale a 1 segundo. O terceiro parâmetro representa a faixa de tempo em que a tarefa será novamente executada. Neste caso, a cada 60 segundos.

A classe AlertTask
Vamos trabalhar com o conceito de classe interna para criar a classe de tarefas. Essa classe deverá estender java.util.TimerTask, a qual fornece o recurso de threads para se trabalhar com agendamentos.
Teremos em AlertTask três variáveis de instancia, a lastDate e context que recebemos como argumento do método construtor e a count que vamos usar para receber do banco de dados a quantidade de novas promoções cadastradas.
```java
class AlertTask extends TimerTask {    
	private LocalDateTime lastDate;
	private WebContext context;
	private long count;
	
	public AlertTask(WebContext context, LocalDateTime lastDate) {
		super();
		this.lastDate = lastDate;
		this.context = context;
	}
 
	@Override
	public void run() {
	    String session = context.getScriptSession().getId();    		
	    Browser.withSession(context, session, new Runnable() {    			
		@Override
		public void run() {
	            Map<String, Object> map =     						
		            repository.totalAndUltimaPromocaoByDataCadastro(lastDate);
		    
                    count = (Long) map.get("count");
		    lastDate = map.get("lastDate") == null 
				? lastDate 
		        	: (LocalDateTime) map.get("lastDate");
				
	            if (count > 0) {
		        ScriptSessions.addFunctionCall("showButton", count);
	            }
                }					
           });
        }		
} 
```
Como a TimerTask é uma classe de thread, vamos precisar implementar o run(). Assim, cada cliente que se conectar ao método init() cai ter sua própria thread, ou seja, seu próprio agendamento.

No método run() de TimerTask, vamos recuperar o id da sessão referente ao cliente que está conectado a canal. Cada cliente terá um id diferente e é por esse id que a DWR saberá qual cliente deve receber a informação enviada pelo servidor. Recuperamos o id por meio da variável context, como se pode ver no código.

O próximo passo é preparar a DWR para o envio da mensagem ao cliente. Isso é feito com o uso da classe Browser e do método withSession(). Esse método terá três parâmetros: o context, o id da sessão (session) e uma thread do tipo Runnable declarada como um classe anonima. Esta classe terá um método run() para executar a thread. E nessa thread é onde teremos o processo com o envio da mensagem para o cliente.

Nessa parte, o primeiro passo será verificar se existem novas promoções cadastradas desde que o cliente se conectou ao servidor pelo canal de comunicação. Para isso, usamos a consulta totalAndUltimaPromocaoByDataCadastro() passando como parâmetro a variável lastDate. Esta variável, supondo que esta consulta ainda não foi executada nenhum vez, terá como valor a data da promoção mais recente quando página foi aberta no navegador.

O retorno da consulta terá dois valores. Um será o total de promoções cadastradas com data superior ao parâmetro lastDate. O segundo será a data mais recente entre as novas promoções encontradas na base de dados. O total de promoções será recuperado e atribuído a variável count.
Já a data será atribuída a variável lastDate, substituindo o valor anterior. Assim, a próxima vez que a consulta for executada, a busca por novas promoções será por datas superiores a esta. Desta forma, sempre vamos buscar novas promoções. Caso não existam novas promoções, count valerá 0 e lastDate será mantida com a mesma data usada como parâmetro na consulta.

Entretanto, caso haja novas promoções, vamos usar a classe ScriptSessions e seu método addFunctionCall() para enviar a mensagem ao cliente. No método devemos passar dois parâmetros. O primeiro é o nome da função JS no lado cliente que espera pela mensagem, esse ainda não criamos, veremos logo mais. Já o segundo é a mensagem que queremos enviar. Nesse caso, a quantidade de novas promoções.

O método showButton
Quando a DWR enviar uma mensagem do servidor para o cliente ela fará esse processo pela seguinte instrução na classe AlertTask:

ScriptSessions.addFunctionCall("showButton", count);
O parâmetro showButton indica o nome da função JavaScript no cliente que vai receber esta mensagem. Essa função deve estar declara no arquivo promo-list.js, como descrita abaixo:
```
var totalOfertas= 0; 
function showButton(count) {
    totalOfertas = totalOfertas + count;
    $("#btn-alert").show(function() {
    	$(this)
            .attr("style", "display: block;")
            .text("Veja " + totalOfertas + " nova(s) oferta(s)!");
    });
}
```
showButton vai receber a mensagem, contento a quantidade de novas promoções, pela variável count. Como lógica dessa função temos as seguintes operações:

o valor de count será incrementado a variável global totalOfertas. Assim, poderemos acessar totalOfertas com o valor atualizado de novas promoções caso o usuário não clique no botão para visualizar as novas ofertas logo que a mensagem surgir na página. Ou seja, suponha que as 10:30 o cliente recebe a mensagem com 2 novas promoções e o usuário não clicou no botão. As 10:32 ele recebe do servidor que novas 3 promoções foram cadastradas, assim, totalOfertas agora vale 5 e esse valor será atualizado no botão.

após receber a mensagem do servidor e incrementar a variável totoalOfertas devemos exibir na página o botão com a quantidade de novas ofertas. Essa ação é feita via jQuery, utilizando a função show().

O clique no botão
Depois que o jQuery exibir o botão na página com a quantidade de novas promoções o usuário vai precisar clicar neste botão para que estas promoções sejam listadas. Para esse processo voltamos ao trabalho com requisições Ajax tradicionais via jQuery por meio da seguinte função:
```
$("#btn-alert").on(“click”, function() {
    $.ajax({
	method: "GET",
	url: "/promocao/list/ajax",
	data: {
		page : 0
	},
	beforeSend: function() {
		pageNumber = 0;
		totalOfertas = 0;
		$("#fim-btn").hide();
		$("#loader-img").addClass("loader");
		$("#btn-alert").attr("style", "display: none;");
		$(".row").fadeOut(400, function(){
			$(this).empty();
		});
	},
	success: function(response) {
		$("#loader-img").removeClass("loader");
		$(".row").fadeIn(250, function(){
			$(this).append(response);
		});
	},
	error: function(xhr) {
		alert("Ops, algo deu errado: " 
				+ xhr.status + ", " + xhr.statusText);
	}
    });
});
```

O processo nesta parte não precisa ser explicado, neste ponto do curso você já deve estar craque e entende-lo perfeitamente. O único passo que vou destacar é na função beforeSend, onde temos a instrução que após o clique no botão desaparece com ele da tela.

```
$("#btn-alert").attr("style", "display: none;");
```
Essa parte poderia inclusive ser adicionada na função de sucesso. Talvez fosse até mais apropriado.



Ok, finalizamos por aqui, até o próximo curso :D

Referencias
Direct Web Remoting: http://directwebremoting.org/dwr/index.html

Download DWR: http://directwebremoting.org/dwr/downloads/index.html

DWR and Spring: http://directwebremoting.org/dwr/documentation/server/integration/spring.html

DWR Reverse Ajax - http://directwebremoting.org/dwr/documentation/reverse-ajax/index.html

Improving Alerts With Reverse Ajax (Part 1) - https://dzone.com/articles/improving-alerts-with-reverse-ajax-part-1

Improving Alerts With Reverse AJAX (Part 2) - https://dzone.com/articles/improving-alerts-with-reverse-ajax-part-2

CreateReverseAjaxWebAppsWithDWR - http://wiki.netbeans.org/CreateReverseAjaxWebAppsWithDWR

Ajax Reverso baseado em Comet com DWR - http://www.univale.com.br/unisite/mundo-j/artigos/47AjaxReverso.pdf

Código Fonte
Caso tenha tido algum tipo de dificuldade para acompanhar a desenvolvimento do código fonte até o final desta seção, ele está disponível na área de arquivo para download.
