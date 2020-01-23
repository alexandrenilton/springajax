var pageNumber = 0;

$(document).ready(function () {
    $("#loader-img").hide();
    $("#fim-btn").hide();

});

// efeito infinity scroll
$(window).scroll(function () {
    var scrollTop = $(this).scrollTop();
    var conteudo = $(document).height() - $(window).height();
    // console.log('scrollTop: ', scrollTop, ' | ', 'conteudo ', conteudo);
    //chama proxima pagina
    if (scrollTop >= conteudo) {
        // console.log("***");
        pageNumber++;
        setTimeout(function () {
            loadByScrollBar(pageNumber);
        }, 200)
    }
});


function loadByScrollBar(pageNumber) {
    var site = $('#autocomplete-input').val();
    $.ajax({
        method: 'GET',
        url: '/promocao/list/ajax',
        data: {
            page: pageNumber,
            site: site
            // passa a variável "page" com o valor de pageNumber para no RequeestForm da rota /promocao/list/ajax
        },
        beforeSend: function () {
            $('#loader-img').show();
        },
        success: function (response) {
            // console.log('resposta.: ', response);
            if (response.length > 150) {
                $('.row').fadeIn(250, function () {
                    $(this).append(response);
                })
            } else {
                $('#fim-btn').show();
                $('#loader-img').removeClass('loader'); // evitar bug de ficar aparecendo o loader no scroll no fim da page
            }
        },
        error: function () {
            alert('Ops! Ocorreu um erro ' + xhr.status + ' - ' + xhr.statusText);
        },
        complete: function () {
            $('#loader-img').hide();
        }
    })
}

// autocomplete
$('#autocomplete-input').autocomplete({
    source: function(request, response) {
        $.ajax({
            method: 'GET',
            url: '/promocao/site',
            data: {
                termo: request.term
            },
            success: function(result) {
                response(result);
            }
        })
    }
});

// botao confirmar
$('#autocomplete-submit').on('click', function() {
    var site = $('#autocomplete-input').val(); //pega valor selecionado
    $.ajax({
        method: 'GET',
        url: '/promocao/site/list',
        data: {
            site: site
        },
        beforeSend: function() {
            pageNumber = 0; // reseta o page number
            $('#fim-btn').hide(); // some com o botao final
            // sumir com os cards antigos
            $('.row').fadeOut(400, function(){
                $(this).empty(); // limpa tudo no div..
            });
        },
        success: function(response) {
            // inserir novos cards
            $('.row').fadeIn(250, function() {
                $(this).append(response);
            });
        },
        error: function(xhr){
            alert('Ops, ocorreu um erro: ' + xhr.status + ' - ' + xhr.statusText);
        }
    })
});

// adicionar likes
// $("button[id*='likes-btn-']").on('click', function() {
$(document).on( 'click', "button[id*='likes-btn-']", function() {
    var id = $(this).attr('id').split('-')[2];
    console.log('id.: ', id);

    $.ajax({
        method: 'POST',
        url: '/promocao/like/' + id,
        success: function(response) {
            $('#likes-count-' + id).text(response);
        },
        error: function(xhr) {
            alert('Ops, ocorreu um erro: ' + xhr.status + ' - ' + xhr.statusText);
        }
    })
});


// AJAX REVERSE
var totalOffers = 0;

$(document).ready(function(){
   init();
});

function init() {
    console.log('dwr init...');

    dwr.engine.setActiveReverseAjax(true);
    dwr.engine.setErrorHandler(fnError);

    DWRAlertPromo.init(); // mesmo nome da classe no pacote web.dwr
}

function fnError(exception) {
    console.log('dwr error: ' + exception);
}

function showButton(count) {
    totalOffers = totalOffers + count;
    $('#btn-alert').show(function() {
        $(this).attr('style', 'display:block;')
            .text('See ' + totalOffers + ' new offers!');
    })
}








$('#btn-alert').on('click', function() {
    $.ajax({
        method: 'GET',
        url: '/promocao/list/ajax',
        data: {
            page: 0
        },
        beforeSend: function() {
            pageNumber = 0; // reseta o page number
            totalOffers = 0;
            $('#fim-btn').hide(); // some com o botao final
            $('#loader-img').addClass('loader');
            $('#btn-alert').attr('style', 'display:none;');
            // sumir com os cards antigos
            $('.row').fadeOut(400, function(){
                $(this).empty(); // limpa tudo no div..
            });
        },
        success: function(response) {
            $('#loader-img').removeClass('loader');
            // inserir novos cards
            $('.row').fadeIn(250, function() {
                $(this).append(response);
            });
        },
        error: function(xhr){
            alert('Ops, ocorreu um erro: ' + xhr.status + ' - ' + xhr.statusText);
        }
    })
});