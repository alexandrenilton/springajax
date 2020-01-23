// submit do form para o controller
$('#form-add-promo').submit(function(evt) {
    // bloquear o comportamento padrão do submit
    evt.preventDefault();

    var promo = {};
    promo.linkPromocao = $("#linkPromocao").val();
    promo.descricao = $("#descricao").val();
    promo.preco = $("#preco").val();
    promo.titulo = $("#titulo").val();
    promo.categoria = $("#categoria").val();
    promo.linkImagem = $("#linkImagem").attr("src");
    promo.site = $("#site").text();
    // console.log('promo ---> ', promo);

    $.ajax({
        method: 'POST',
        url: '/promocao/save',
        data: promo,
        beforeSend: function(){
            // removendo as mensagens
            $('span').closest('.error-span').remove(); // remove todos os spans com a classe .error-span
            // remover as bordas vermelhas
            $('#categoria').removeClass('is-invalid');
            $('#preco').removeClass('is-invalid');
            $('#linkPromocao').removeClass('is-invalid');
            $('#titulo').removeClass('is-invalid');
            // habilitar load
            $('#form-add-promo').hide();
            $('#loader-form').addClass("loader").show();
        },

        success: function() {
            $('#form-add-promo').each(function() {
               this.reset(); // reseta todos os components de input
            });
            $('#linkImagem').attr('src', '/images/promo-dark.png');
            $('#site').text("");
            $('#alert')
                .removeClass('alert alert-danger')
                .addClass('alert alert-success')
                .text('Ok! Promoção cadastrada com sucesso.');
        },
        statusCode: {
            422: function(xhr) {
                console.log('status error: ', xhr.status);
                var errors = $.parseJSON(xhr.responseText);
                $.each(errors, function(key, val){
                    $('#' + key).addClass('is-invalid');
                    $('#error-' + key)
                        .addClass('invalid-feedback')
                        .append("<span class='error-span'>" + val + "</span>");
                });
            }
        },
        error: function(xhr) {
            console.log('--> error: ', xhr.responseText);
            $('#alert').addClass('alert alert-danger').text('Não foi possivel salvar esta promoção');
        },
        complete: function() {
            $('#loader-form').fadeOut(800, function() {
               $("#form-add-promo").fadeIn(500) ;
               $("#loader-form").removeClass("loader");
            });
        }
    })
});


// function to capture meta tags
$('#linkPromocao').on('change', function () {
    var url = $(this).val();

    // http:// (7 characters)
    if ( url.length > 7 ) {
        $.ajax({
            method: 'POST',
            url: '/meta/info?url=' + url,
            cache: false,
            beforeSend: function() {
                // limpar execucoes anteriores
                $('#alert').removeClass('alert alert-danger alert-success').text('');
                $('#titulo').val("");
                $('#site').text("");

                // adicionar animação de load
                $('#loader-img').addClass("loader");
                $('#linkImagem').attr("src", "");
            },
            success: function (data) {
                // console.log(data);
                $('#titulo').val(data.title);
                $('#site').text(data.site.replace('@', '')); // tirar o @
                $('#linkImagem').attr('src', data.image);
            },
            statusCode: {
                404: function() {
                    $("#alert")
                        .addClass("alert alert-danger")
                        .text("Nenhuma informação pode ser recuperada dessa url.");
                },
                500: function() {
                    $("#alert")
                        .addClass("alert alert-danger")
                        .text("Ops... algo deu errado, tente mais tarde.");
                }
            },
            error: function() {
                $('alert').addClass('alert alert-danger').text("Erro ao tentar buscar informações na URL fornecida");
                $('#linkImagem').attr("src", "/images/promo-dark.png");
            },
            complete: function() {
                // remover a animação do load
                $('#loader-img').removeClass("loader");
            }
        });
    }
});